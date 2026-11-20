package com.campus.trade.controller;


import com.campus.trade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/api/mq")

public class MqPingController {

    public static final String QUEUE = "mq.ping";
    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/ping")
    public Result<String> ping() {
       String body = "ping-"+System.currentTimeMillis();
       rabbitTemplate.convertAndSend(QUEUE, body);
       Object got=rabbitTemplate.receiveAndConvert(QUEUE);
       return Result.ok("sent="+body+",received"+got);

    }

}
