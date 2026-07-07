package com.example.shop.domain.entity;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 购物车明细查询结果。
 */
@Data
public class CartItemDetail {

    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Boolean selected;
    private String productName;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String description;
}
