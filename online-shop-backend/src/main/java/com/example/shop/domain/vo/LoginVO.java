package com.example.shop.domain.vo;

import lombok.Data;

/**
 * 登录响应。
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
}
