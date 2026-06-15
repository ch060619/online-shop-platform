package com.example.shop.common;

import java.util.Locale;

/**
 * 用户角色。
 */
public enum UserRole {

    /**
     * 普通用户角色。
     */
    USER,

    /**
     * 管理员角色。
     */
    ADMIN;

    /**
     * 解析数据库或令牌中的角色值。
     *
     * @param value 角色字符串
     * @return 用户角色
     */
    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        return UserRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
