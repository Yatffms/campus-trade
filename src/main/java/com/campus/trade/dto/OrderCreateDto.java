package com.campus.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDto {
    @NotNull(message = "商品ID不能为空")
    private Long itemId;

}
