package com.example.shop.domain.vo;

import lombok.Data;

/**
 * 登录响应。
 */
@Data
public class LoginVO {

    private String token;
    private String accessToken;
    private String refreshToken;
    private Long expiresInSeconds;
    private String tokenType;
    private String role;
    private Long userId;
    private String username;
    private String nickname;
}
