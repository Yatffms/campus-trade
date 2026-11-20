package com.campus.trade.VO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class OrderVO {

    private Long id;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private Long buyerId;
    private Long sellerId;
    private Integer status;
    private BigDecimal price;
    private LocalDateTime createTime;

    private Integer payStatus;
    private LocalDateTime payTime;
    private String payNo;



    private String sellerNickname;
    private String buyerNickname;

    private LocalDateTime expireTime;
}
