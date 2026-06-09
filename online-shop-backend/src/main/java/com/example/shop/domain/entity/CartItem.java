package com.example.shop.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 购物车明细实体。
 */
@Data
public class CartItem {

    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
