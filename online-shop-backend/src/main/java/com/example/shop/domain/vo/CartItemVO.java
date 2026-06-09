package com.example.shop.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 购物车明细响应对象。
 */
@Data
public class CartItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private String imageUrl;
    private BigDecimal subtotal;
}
