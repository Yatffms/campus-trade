package com.campus.trade.service;


import java.util.Map;

public interface PayService  {

    String pagePayForm(Long orderId);
    PayCheckResult queryAndMarkPaid(String outTradeNo);


    String handleNotify(Map<String, String> params);
}
