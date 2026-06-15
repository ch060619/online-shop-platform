package com.example.shop.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 订单摘要响应对象。
 */
@Data
public class OrderSummaryVO {

    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private LocalDateTime paidAt;
}
