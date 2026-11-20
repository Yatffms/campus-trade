package com.campus.trade.Task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.entity.Order;
import com.campus.trade.mapper.OrderMapper;
import com.campus.trade.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class  OrderTimeoutTask  {
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderService orderService;

    @Scheduled(fixedDelay = 600_000 )//每10分钟 注意 这里原来是60_000 也就是1分钟现在有了Rabbit 消息队列，所以这里可以设置长一些 只是一个兜底的作用
    public  void  cancelExpired(){
        List<Order> list = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order:: getStatus, 0)
                        .le(Order::getExpireTime, LocalDateTime.now())
                        .last("limit 100")
        );
        for (Order order : list)
            try{
                orderService.timeoutCancel(order.getId());
            }catch (Exception e){
                log.warn("超时取消失败 orderId={}", order.getId(), e);
            }

    }
}
