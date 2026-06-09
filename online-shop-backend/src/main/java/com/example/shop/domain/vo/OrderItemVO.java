package com.example.shop.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细响应对象。
 */
@Data
public class OrderItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
