package com.campus.trade.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemCreateDto {

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题不能超过64个字符")
    private  String title;

    @Size(max = 1000, message = "描述不能超过1000个字符")
    private  String description;

    @NotBlank(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private  String price;

    @DecimalMin(value = "0.00", inclusive = false, message = "原价必须大于0")
    private  BigDecimal originalPrice;


    private  String coverImage;
    private  String school;
    private  Long   categoryId;
}
