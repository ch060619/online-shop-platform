package com.example.shop.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改购物车数量请求。
 */
@Data
public class UpdateCartItemRequest {

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于 0")
    private Integer quantity;
}
