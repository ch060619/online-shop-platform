package com.example.shop.common;

/**
 * 实验版用户上下文。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前用户 ID。
     *
     * @param userId 用户 ID
     */
    public static void setCurrentUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 当前用户 ID
     */
    public static Long getCurrentUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            return 1L;
        }
        return userId;
    }

    /**
     * 清理当前线程用户上下文。
     */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
