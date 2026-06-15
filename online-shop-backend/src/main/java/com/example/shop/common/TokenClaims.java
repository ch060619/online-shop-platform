package com.example.shop.common;

/**
 * 登录令牌载荷。
 *
 * @param userId 用户 ID
 * @param role 用户角色
 * @param expiresAtEpochSeconds 过期时间戳，单位秒
 */
public record TokenClaims(Long userId, String role, long expiresAtEpochSeconds) {
}
