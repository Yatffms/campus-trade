package com.campus.trade.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ItemVO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private String school;
    private Long categoryId;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;


    private Long sellerId;  //后面补的
    private String sellerNickname;
    private String sellerAvatar;
}
