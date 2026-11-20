package com.campus.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Data
public class MessageCreateDto {
    @NotNull(message = "商品id不能为空")
    private Long itemId;

    @NotNull(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容不能超过500个字符")
    private String content;
}
