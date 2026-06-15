package com.example.shop.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 刷新令牌实体。
 */
@Data
public class RefreshToken {

    private Long id;
    private String tokenHash;
    private Long userId;
    private LocalDateTime expiresAt;
    private Boolean revoked;
    private LocalDateTime createdAt;
}
