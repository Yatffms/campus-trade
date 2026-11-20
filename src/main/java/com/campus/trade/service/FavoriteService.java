package com.campus.trade.service;

import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;

public interface FavoriteService {
    void add(Long itemId);
    void remove(Long itemId);
    PageResult <ItemVO> page (int  page,int size );
}
