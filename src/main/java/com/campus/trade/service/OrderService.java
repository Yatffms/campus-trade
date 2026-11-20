package com.campus.trade.service;

import com.campus.trade.VO.PageResult;
import com.campus.trade.VO.OrderVO;

public interface OrderService  {
    Long  create (Long itemId);
    void  confirm (Long orderId);
    void  cancel (Long orderId);
    void  finish (Long orderId);
    PageResult<OrderVO>  pageBought (int page, int size);
    PageResult<OrderVO>  pageSold (int page, int size);
    void timeoutCancel( Long orderId);
}
