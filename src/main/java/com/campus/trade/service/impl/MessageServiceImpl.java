package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.VO.MessageVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.UserContext;
import com.campus.trade.dto.MessageCreateDto;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.Message;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.MessageMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl  implements MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private ItemMapper itemMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public void send(MessageCreateDto dto) {
        Item item = itemMapper.selectById(dto.getItemId());
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }

        Long userId = UserContext.getUserId();
        if (item.getSellerId()==userId) {
            throw new BusinessException(400, "不能给自己的商品留言");
        }

        Message message = new Message();
        message.setItemId(item.getId());
        message.setFromUserId(userId);
        message.setToUserId(item.getSellerId());
        message.setContent(dto.getContent());
        message.setIsRead(0);
        messageMapper.insert(message);
    }
    @Override
    public PageResult<MessageVO> pageByItem(Long itemId, int page, int size) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (size > 50) {
            size = 50;
        }

        Page<Message> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getItemId, itemId)
                .orderByDesc(Message::getCreateTime);
        Page<Message> messagePage = messageMapper.selectPage(pageParam, wrapper);

        List<Long> userIds = messagePage.getRecords().stream()
                .map(Message::getFromUserId)
                .distinct()
                .toList();


        Map<Long, User> userMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<MessageVO> records = messagePage.getRecords().stream()
                .map(m -> toVO(m, userMap.get(m.getFromUserId()), item))
                .toList();

        PageResult<MessageVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(messagePage.getTotal());
        result.setSize(messagePage.getSize());
        result.setPage(messagePage.getCurrent());
        result.setPages(messagePage.getPages());
        return result;
    }

    @Override
    public PageResult<MessageVO> pageReceived(int page, int size) {
        if(size > 50)
            size = 50;
        Page<Message>  pageParam=new Page<>(page, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getToUserId, UserContext.getUserId())
                .orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = messageMapper.selectPage(pageParam, wrapper);
        List<Message>messages=messagePage.getRecords();

        List<Long> userIds = messages.stream()
                .map(Message::getFromUserId)
                .distinct()
                .toList();
        List<Long>itemIds= messages.stream()
                .map(Message::getItemId)
                .distinct()
                .toList();
        Map<Long,   User> userMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long,Item> itemMap = itemIds.isEmpty()
                ? Map.of()
                : itemMapper.selectBatchIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));
        List<MessageVO>records=messages.stream()
                .map(m -> toVO(m, userMap.get(m.getFromUserId()), itemMap.get(m.getItemId())))
                .toList();

        PageResult<MessageVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(messagePage.getTotal());
        result.setSize(messagePage.getSize());
        result.setPage(messagePage.getCurrent());
        result.setPages(messagePage.getPages());


        return result;
    }

    @Override
    public Long unReadCount() {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, UserContext.getUserId())
                .eq(Message::getIsRead, 0));
        return count==null? 0L : count;
    }

    @Override
    public void markRead(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "留言不存在");
        }
        if (!Objects.equals(message.getToUserId(), UserContext.getUserId())) {
            throw new BusinessException(403, "只能操作自己收到的留言");
        }
        if(Integer.valueOf(1).equals(message.getIsRead()))
            return ;//直接就是已读
        message.setIsRead(1);
        messageMapper.updateById(message);

    }

    @Override
    public void markAllRead() {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getToUserId, UserContext.getUserId())
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .set(Message::getUpdateTime, LocalDateTime.now());
        messageMapper.update(null, wrapper);
    }

    private MessageVO toVO(Message m,  User fromUser) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setItemId(m.getItemId());
        vo.setFromUserId(m.getFromUserId());
        if (fromUser != null) {
            vo.setFromNickName(fromUser.getNickname());
            vo.setFromAvatar(fromUser.getAvatar());
        }
        vo.setContent(m.getContent());
        vo.setCreateTime(m.getCreateTime());

        vo.setIsRead(m.getIsRead());

        return vo;
    }
    //新增重载
    private MessageVO toVO(Message m, User fromUser, Item item) {
        MessageVO vo = toVO(m, fromUser);
        if (item !=null) {
            vo.setItemTitle(item.getTitle());
        }
        return vo;
    }



}
