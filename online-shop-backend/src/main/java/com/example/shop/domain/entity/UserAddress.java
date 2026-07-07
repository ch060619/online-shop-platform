package com.example.shop.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户收货地址实体。
 */
@Data
public class UserAddress {

    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Boolean defaultAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
