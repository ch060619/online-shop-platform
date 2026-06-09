package com.example.shop.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车请求。
 */
@Data
public class AddCartItemRequest {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于 0")
    private Integer quantity;
}
