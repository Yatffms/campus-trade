package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.VO.ItemVO;
import com.campus.trade.VO.PageResult;
import com.campus.trade.dto.ItemCreateDto;
import com.campus.trade.entity.Item;

public interface ItemService {
    Long publish(ItemCreateDto dto);
   // Page<Item> page (int page , int size, String keyword ,Long categoryId) ;
   // Item getDetail(Long id);
    PageResult<ItemVO> page (int page , int size, String keyword , Long categoryId) ;
    ItemVO getDetail(Long id);

    void updateStatus(Long itemId, Integer status);
    void edit (Long itemId, ItemCreateDto dto) ;
}
