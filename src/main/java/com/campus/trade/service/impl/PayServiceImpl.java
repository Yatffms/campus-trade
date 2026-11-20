package com.campus.trade.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.UserContext;
import com.campus.trade.config.AlipayConfig;
import com.campus.trade.entity.Order;
import com.campus.trade.mapper.OrderMapper;
import com.campus.trade.service.PayCheckResult;
import com.campus.trade.service.PayService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PayServiceImpl  implements PayService {

    private static final String OUT_TRADE_NO_PREFIX = "CT";

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipayConfig alipayConfig;

    @Resource
    private OrderMapper orderMapper;





    @Override
    public String pagePayForm(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null)
            throw new BusinessException(404, "订单不存在");

        if (!Objects.equals(order.getBuyerId(), UserContext.getUserId()))
            throw new BusinessException(403, "只能支付自己的订单");

        if (Integer.valueOf(1).equals(order.getPayStatus()))
            throw new BusinessException(400, "订单已支付，请勿重复支付");

        if (!Integer.valueOf(0).equals(order.getStatus()))
            throw new BusinessException(400, "订单已取消或已完成，无法支付");


        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(400, "订单已超时，请重新下单");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        if (StringUtils.hasText(alipayConfig.getNotifyUrl()))
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

        String outTradeNo = OUT_TRADE_NO_PREFIX + order.getId();
        request.setBizContent("{"
                + "\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + order.getPrice().toPlainString() + "\","
                + "\"subject\":\"校园二手交易订单 " + outTradeNo + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}");
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (Exception e) {
            throw new BusinessException(500, "支付请求失败" + e.getMessage());
        }
    }


    @Override
    public PayCheckResult queryAndMarkPaid(String outTradeNo) {
        Long orderId = parseOrderId(outTradeNo);
        AlipayTradeQueryResponse resp;
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");
            resp = alipayClient.execute(request);
        } catch (Exception e) {
            throw new BusinessException(500, "查询支付宝订单状态失败" + e.getMessage());
        }

        boolean paid = "10000".equals(resp.getCode())
                && ("TRADE_SUCCESS".equals(resp.getTradeStatus())
                || "TRADE_FINISHED".equals(resp.getTradeStatus()));
        if (!paid) {
            return new PayCheckResult(resp, false,  orderId);
        }
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getPayStatus, 0)
                        .set(Order::getPayStatus, 1)
                         .set(Order::getPayTime, LocalDateTime.now())
                        .set(Order::getPayNo, resp.getTradeNo()));
        return new PayCheckResult(resp, rows == 1, orderId);
    }


    @Override
    public String handleNotify(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        try {
            // 1. 验签：用支付宝公钥验证，防伪造通知
            if (!AlipaySignature.rsaCheckV1(params, alipayConfig.publicKey(), "utf-8", "RSA2")) {
                log.warn("异步通知验签失败 outTradeNo={}", outTradeNo);
                return "failure";
            }
            // 2. 确认通知是发给本应用的
            if (!Objects.equals(alipayConfig.getAppId(), params.get("app_id"))) {
                log.warn("异步通知 app_id 不匹配 appId={}", params.get("app_id"));
                return "failure";
            }
            // 3. 只处理支付成功，其它状态回 success（表示收到，别再推）
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.info("异步通知忽略非成功状态 outTradeNo={} tradeStatus={}", outTradeNo, tradeStatus);
                return "success";
            }
            // 4. 金额必须与订单一致
            Long orderId = parseOrderId(outTradeNo);
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.warn("异步通知订单不存在 orderId={}", orderId);
                return "failure";
            }
            BigDecimal notifyAmount = new BigDecimal(params.get("total_amount"));
            if (order.getPrice() == null || order.getPrice().compareTo(notifyAmount) != 0) {
                log.error("异步通知金额不匹配 orderId={} db={} notify={}", orderId, order.getPrice(), notifyAmount);
                return "failure";
            }
            // 5. 幂等落库：只有 pay_status 还是 0 才更新
            int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, orderId)
                    .eq(Order::getPayStatus, 0)
                    .set(Order::getPayStatus, 1)
                    .set(Order::getPayTime, LocalDateTime.now())
                    .set(Order::getPayNo, params.get("trade_no")));
            log.info("异步通知处理完成 orderId={} rows={}", orderId, rows);
            return "success";
        } catch (Exception e) {
            log.error("异步通知处理异常 outTradeNo={}", outTradeNo, e);
            return "failure";
        }
    }













    private Long parseOrderId(String outTradeNo) {
        if (outTradeNo == null || !outTradeNo.startsWith(OUT_TRADE_NO_PREFIX)) {
            throw new BusinessException(400, "非法的订单号");
        }
        try {
            return Long.parseLong(outTradeNo.substring(OUT_TRADE_NO_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "非法的订单号");
        }

    }


}
