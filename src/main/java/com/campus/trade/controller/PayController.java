package com.campus.trade.controller;


import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.campus.trade.common.Result;
import com.campus.trade.config.AlipayConfig;
import com.campus.trade.service.PayCheckResult;
import com.campus.trade.service.PayService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@RestController
@RequestMapping("/api/pay")

public class  PayController {


    @Resource
    private AlipayClient alipayClient; // 支付宝客户端


    @GetMapping("/ping")
    public Result<Map<String, String>> ping() throws Exception {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"PING20261111\"}");
        AlipayTradeQueryResponse response = alipayClient.execute(request);

        Map<String, String> data = new LinkedHashMap<>();
        data.put("code", response.getCode());
        data.put("subCode", response.getSubCode());
        data.put("subMsg", response.getSubMsg());
        return Result.ok(data);
    }


    @Resource
    private PayService payService;
    @Resource
    private AlipayConfig alipayConfig;//方便拿url

//    @GetMapping("/page")
//    public void pagePay(@RequestParam(defaultValue="0.01") String amount,
//                        HttpServletResponse response)throws Exception{
//        AlipayTradePagePayRequest request=new AlipayTradePagePayRequest();
//        request.setReturnUrl(alipayConfig.getReturnUrl());  //付款之后跳转
//        if (StringUtils.hasText(alipayConfig.getNotifyUrl()))   {
//            request.setNotifyUrl(alipayConfig.getNotifyUrl());
//        }
//        String outTradeNo="TEST"+System.currentTimeMillis();
//        request.setBizContent("{"
//                + "\"out_trade_no\":\""+outTradeNo+"\","
//                + "\"total_amount\":\""+amount+"\","
//                + "\"subject\":\"TEST\","
//                + "\"body\":\"TEST\","
//                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
//                + "}");
//        String form = alipayClient.pageExecute(request).getBody();
//        response.setContentType("text/html;charset=utf-8");
//        response.getWriter().write(form);
//    }

    @GetMapping("/page")
    public void pagePay(@RequestParam Long orderId, HttpServletResponse response) throws Exception {
        String html = payService.pagePayForm(orderId);   // 先调 service，异常在这里抛
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(html);
    }


    @GetMapping(value = "/return", produces = MediaType.TEXT_HTML_VALUE)
    public String payReturn(@RequestParam(value = "out_trade_no", required = false) String outTradeNo,
                            @RequestParam(value = "trade_no", required = false) String tradeNo) throws Exception {
        if (!StringUtils.hasText(outTradeNo))
            return "<h2>没有拿到 out_trade_no</h2>";


//        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//        request.setBizContent("{\"out_trade_no\":\""+outTradeNo+"\"}");
//        AlipayTradeQueryResponse resp = alipayClient.execute(request);
        //这一行可以删掉了查单已经在 payService.queryAndMarkPaid() 里做过了


        PayCheckResult check = payService.queryAndMarkPaid(outTradeNo);
        AlipayTradeQueryResponse resp = check.resp();


        boolean paid = "10000".equals(resp.getCode())
                && ("TRADE_SUCCESS".equals(resp.getTradeStatus()));

        StringBuilder html = new StringBuilder();
//        html.append("<!doctype html><meta charset='utf-8'><title>支付结果</title>");
//        html.append("<h2>").append(paid ? "支付成功（已向支付宝查单确认）" : "未确认支付").append("</h2>");
//        html.append("<table border='1' cellpadding='6' style='border-collapse:collapse'>");
//        html.append("<tr><th>回跳参数 out_trade_no</th><td>").append(outTradeNo).append("</td></tr>");
//        html.append("<tr><th>回跳参数 trade_no</th><td>").append(tradeNo).append("</td></tr>");
//        html.append("<tr><th>查单 code</th><td>").append(resp.getCode()).append("</td></tr>");
//        html.append("<tr><th>查单 sub_code</th><td>").append(resp.getSubCode()).append("</td></tr>");
//        html.append("<tr><th>trade_status</th><td>").append(resp.getTradeStatus()).append("</td></tr>");
//        html.append("<tr><th>total_amount</th><td>").append(resp.getTotalAmount()).append("</td></tr>");
//        html.append("<tr><th>支付宝 trade_no</th><td>").append(resp.getTradeNo()).append("</td></tr>");
//        html.append("<tr><th>买家账号</th><td>").append(resp.getBuyerLogonId()).append("</td></tr>");
//        html.append("</table>");
        html.append("<!doctype html><meta charset='utf-8'><title>支付结果</title>");
        html.append("<h2>").append(paid ? "支付成功（已向支付宝查单确认）" : "未确认支付").append("</h2>");
        html.append("<p>").append(check.firstTime()
                ? "本次已把订单标记为已支付（pay_status 0 → 1）"
                : "订单此前已处理过，本次没有重复写入（幂等生效）").append("</p>");
        html.append("<table border='1' cellpadding='6' style='border-collapse:collapse'>");
        html.append("<tr><th>订单号</th><td>").append(String.valueOf(check.orderId())).append("</td></tr>");
        html.append("<tr><th>回跳参数 out_trade_no</th><td>").append(outTradeNo).append("</td></tr>");
        html.append("<tr><th>回跳参数 trade_no</th><td>").append(tradeNo).append("</td></tr>");
        html.append("<tr><th>查单 code</th><td>").append(resp.getCode()).append("</td></tr>");
        html.append("<tr><th>trade_status</th><td>").append(resp.getTradeStatus()).append("</td></tr>");
        html.append("<tr><th>total_amount</th><td>").append(resp.getTotalAmount()).append("</td></tr>");
        html.append("<tr><th>支付宝 trade_no</th><td>").append(resp.getTradeNo()).append("</td></tr>");
        html.append("<tr><th>买家账号</th><td>").append(resp.getBuyerLogonId()).append("</td></tr>");
        html.append("</table>");
        return html.toString();
    }


    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String payNotify(@RequestParam Map<String, String> params) {
        return payService.handleNotify(params);
    }





}



