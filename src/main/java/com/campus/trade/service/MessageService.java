package com.campus.trade.service;

import com.campus.trade.VO.MessageVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.dto.MessageCreateDto;


public interface MessageService {

    void send(MessageCreateDto dto);
    PageResult<MessageVO> pageByItem(Long itemId, int page , int size);
    PageResult<MessageVO> pageReceived( int page , int size);
    Long   unReadCount();
    void markRead(Long id);
    void markAllRead();

}
