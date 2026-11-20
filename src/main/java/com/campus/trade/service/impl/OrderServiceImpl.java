package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.VO.OrderVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.cache.ItemCache;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.UserContext;
import com.campus.trade.config.RabbitConfig;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.Order;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.OrderMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class OrderServiceImpl  implements OrderService{


    @Resource
    private OrderMapper orderMapper;
    @Resource
    private ItemMapper itemMapper;
    @Resource
    private ItemCache itemCache;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserMapper userMapper;


    @Override
    @Transactional
    public Long create(Long itemId) {
       Long buyerId= UserContext.getUserId();
       Item item = itemMapper.selectById(itemId);
       if (item == null)
           throw new BusinessException(404, "商品不存在");
       if (Objects.equals(item.getSellerId(), buyerId))
           throw new BusinessException(400, "不能购买自己的商品");

       if(!Integer.valueOf(1).equals(item.getStatus()))
           throw new BusinessException(400, "商品已下架");
        LambdaUpdateWrapper<Item> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Item::getId, itemId);
        wrapper.eq(Item::getStatus, 1);
        wrapper.set(Item::getStatus, 2);

        int rows = itemMapper.update(null, wrapper);
        if (rows == 0)
            throw new BusinessException(400, "手慢无");
        Order order = new Order();
        order.setItemId(itemId);
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));//加入超时时间
        order.setPrice(item.getPrice());

        orderMapper.insert(order);
        itemCache.evict(itemId);

        rabbitTemplate.convertAndSend(RabbitConfig.DELAY_EXCHANGE, RabbitConfig.DELAY_ROUTING_KEY, String.valueOf(order.getId()));

        return order.getId();
    }

    @Override
    public void confirm(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null)
            throw new BusinessException(404, "订单不存在");
        if(!Objects.equals(order.getSellerId(), UserContext.getUserId()))
            throw new BusinessException(403, "只有卖家可以确认自己的订单");
        if(!Integer.valueOf(0).equals(order.getStatus()))
            throw new BusinessException(400,"当前订单不能状态不能确认");
        if (!Integer.valueOf(1).equals(order.getPayStatus()))
            throw new BusinessException(400, "买家还没付款，不能确认订单");
        int rows = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, 0)
                        .set(Order::getStatus, 1)
                        .eq(Order::getPayStatus, 1));


        if (rows == 0) {
            throw new BusinessException(400, "当前订单状态不能确认");
        }

    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null)
            throw new BusinessException(404, "订单不存在");

        boolean isBuyer = Objects.equals(order.getBuyerId(), userId);
        boolean isSeller = Objects.equals(order.getSellerId(), userId);
        if(!isBuyer && !isSeller)
            throw new BusinessException(403, "无权限取消订单");
        if(!Integer.valueOf(0).equals(order.getStatus())&& !Integer.valueOf(1).equals(order.getStatus()))
            throw new BusinessException(400,"当前订单不能状态不能取消");

        if (Integer.valueOf(1).equals(order.getPayStatus()))
            throw new BusinessException(400, "订单已支付，不能直接取消，需走退款");
        int rows = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .in(Order::getStatus, 0, 1)
                        .set(Order::getStatus, 2));

        if (rows == 0) {
            throw new BusinessException(400, "当前订单状态不能取消");
        }

        Item item = itemMapper.selectById(order.getItemId());
        if(item != null && Integer.valueOf(2).equals(item.getStatus())){
            item.setStatus(1);
            itemMapper.updateById(item);
        }
        itemCache.evict(order.getItemId());
    }

    @Override
    @Transactional
    public void finish(Long orderId) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null)
            throw new BusinessException(404, "订单不存在");
        if(!Objects.equals(order.getBuyerId(), userId))
            throw new BusinessException(403, "只有买家可以确认自己的订单");
        if(!Integer.valueOf(1).equals(order.getStatus()))
            throw new BusinessException(400,"只有已确认的订单才能完成");

        order.setStatus(3);
        orderMapper.updateById(order);

    }

    @Override
    public PageResult<OrderVO> pageSold(int page, int size) {
        int p=Math.max(page, 1);
        int s=Math.min(Math.max(size, 1), 50);

        Page<Order> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getSellerId, UserContext.getUserId());
        qw.orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, qw);

        List<Long> itemIds = result.getRecords().stream()
                .map(Order::getItemId)
                .distinct()
                .toList();

        Map<Long, Item>itemMap=itemIds.isEmpty()
                ? Map.of()
                : itemMapper.selectBatchIds(itemIds).stream()
                       .collect(Collectors.toMap(Item::getId, i -> i));
        Map<Long, User> userMap = loadUsers(result.getRecords());



        List<OrderVO> vos=result.getRecords().stream()
                .map(o -> toVO(o, itemMap.get(o.getItemId()), userMap))
                .toList();

        PageResult <OrderVO> pr=new PageResult<>();
                pr.setRecords(vos);
                pr.setTotal(result.getTotal());
                pr.setSize(result.getSize());
                pr.setPage(result.getCurrent());
                pr.setPages(result.getPages());
        return pr;
    }


    @Override
    public PageResult<OrderVO> pageBought(int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 50);

        Page<Order> pageParam = new Page<>(p, s);
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getBuyerId, UserContext.getUserId())
                .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, qw);

        List<Long> itemIds = result.getRecords().stream()
                .map(Order::getItemId)
                .distinct()
                .toList();

        Map<Long, Item> itemMap = itemIds.isEmpty()
                ? Map.of()
                : itemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));
        Map<Long, User> userMap = loadUsers(result.getRecords());

        List<OrderVO> vos = result.getRecords().stream()
                .map(o -> toVO(o, itemMap.get(o.getItemId()), userMap))
                .toList();

        PageResult<OrderVO> pr = new PageResult<>();
        pr.setRecords(vos);
        pr.setTotal(result.getTotal());
        pr.setSize(result.getSize());
        pr.setPage(result.getCurrent());
        pr.setPages(result.getPages());
        return pr;
    }

    private OrderVO toVO(Order order, Item item, Map<Long, User> userMap) {
        OrderVO vo=new OrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setStatus(order.getStatus());
        vo.setPrice(order.getPrice());
        vo.setCreateTime(order.getCreateTime());

        vo.setExpireTime(order.getExpireTime());

        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(order.getPayTime());
        vo.setPayNo(order.getPayNo());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
            vo.setItemCover(item.getCoverImage());
        }

        User seller = userMap.get(order.getSellerId());
        if (seller != null) vo.setSellerNickname(seller.getNickname());
        User buyer = userMap.get(order.getBuyerId());
        if (buyer != null) vo.setBuyerNickname(buyer.getNickname());


        return vo;

    }


    private Map<Long, User> loadUsers(List<Order> orders) {
        List<Long> ids = orders.stream()
                .flatMap(o -> Stream.of(o.getBuyerId(), o.getSellerId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }




    @Override
    @Transactional
    public void timeoutCancel(Long orderId) {
        Order order=orderMapper.selectById(orderId);
        if(order==null|| !Integer.valueOf(0).equals(order.getStatus()))
            return;
        if (Integer.valueOf(1).equals(order.getPayStatus()))
            return; //买家付了钱、卖家还没点确认，15 分钟一到定时任务就把订单改成“已取消”、商品回到在售——钱收了、货没了。这是这整套支付里最贵的 bug
        int rows=orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getStatus, 0)//注意只有状态0才更新
                        .set(Order::getStatus, 2));//改成已取消
        if (rows == 0)
            return;
        itemMapper.update(null,
                new LambdaUpdateWrapper<Item>()
                        .eq(Item::getId, order.getItemId())
                        .eq(Item::getStatus, 2)
                        .set(Item::getStatus, 1));
        itemCache.evict(order.getItemId());

    }
}
