package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@TableName("orders")
public class Order {


    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;
    private Long buyerId;
    private Long sellerId;
    private Integer status;
    private BigDecimal price;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime  createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    private LocalDateTime expireTime;


    private Integer payStatus;
    private LocalDateTime payTime;
    private String payNo;

}
