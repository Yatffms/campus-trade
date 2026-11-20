package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;
    private Long categoryId;
    private  String title;
    private  String description;

    //金额用BigDecimal 和SQL的DECIMAL对应
    private BigDecimal price;
    private BigDecimal originalPrice;

    private  String coverImage;
    private  String school;

    private  Integer status;//1 在售 0 下架 2已售

    private  Integer viewCount;

    @TableLogic
    private  Integer deleted;

    @TableField(fill = FieldFill.INSERT)//插入时候填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新时候填充  注意这一环节是在第三轮的填充时候加的
    private LocalDateTime updateTime;



}
