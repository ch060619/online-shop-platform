package com.example.shop.domain.vo;

import lombok.Data;

/**
 * 个人中心用户概览响应。
 */
@Data
public class UserProfileVO {

    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String role;
    private Integer points;
    private Integer orderCount;
}
