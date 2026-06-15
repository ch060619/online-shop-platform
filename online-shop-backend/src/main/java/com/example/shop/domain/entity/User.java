package com.example.shop.domain.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户实体。
 */
@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
