package com.example.shop.domain.entity;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细实体。
 */
@Data
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
