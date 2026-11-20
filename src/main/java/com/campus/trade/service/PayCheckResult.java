package com.campus.trade.service;

import com.alipay.api.response.AlipayTradeQueryResponse;

public  record PayCheckResult(AlipayTradeQueryResponse resp, boolean firstTime ,Long orderId) {
}
