package com.campus.trade.mq;

import com.campus.trade.config.RabbitConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.campus.trade.service.OrderService;



@Slf4j
@Component
public class OrderTimeoutListener {

    @Resource
    private OrderService orderService;
    @RabbitListener(queues = RabbitConfig.TIMEOUT_QUEUE)
    public void onTimeout (String orderId){
        log.info("收到延迟消息，取消超时订单，OrderId={}", orderId);
        orderService.timeoutCancel(Long.valueOf(orderId));
    }
}
