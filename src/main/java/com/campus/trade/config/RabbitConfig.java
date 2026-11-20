package com.campus.trade.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




@Configuration
public class RabbitConfig  {
    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "order.delay";
    public static final String DLX_EXCHANGE = "order.dlx.exchange";
    public static final String TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String TIMEOUT_ROUTING_KEY = "order.timeout";

    @Bean
    public Queue mqPingQueue() {
        return new  Queue("mq.ping",false);
    }

    // 交换机
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(DELAY_EXCHANGE, true, false);
    }

    // 队列
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .ttl(15 * 60 * 1000)                 // 15 分钟
                .deadLetterExchange(DLX_EXCHANGE)    // 过期后交给谁
                .deadLetterRoutingKey(TIMEOUT_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with(DELAY_ROUTING_KEY);
    }
    //死信交换机：接受过期的消息
    @Bean
    public DirectExchange orderDlxExchange(){
        return new DirectExchange(DLX_EXCHANGE,true,false);
    }

    //超时处理队列：消费方监听这个
    @Bean
    public Queue orderTimeoutQueue(){
        return QueueBuilder.durable(TIMEOUT_QUEUE).build();

    }
    @Bean
    public Binding orderTimeoutBinding(){
        return BindingBuilder.bind(orderTimeoutQueue()).to(orderDlxExchange()).with(TIMEOUT_ROUTING_KEY);
    }









}
