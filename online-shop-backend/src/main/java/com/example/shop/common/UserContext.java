package com.example.shop.common;

/**
 * 实验版用户上下文。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前用户 ID。
     *
     * @param userId 用户 ID
     */
    public static void setCurrentUserId(Long userId) {
        setCurrentUser(userId, UserRole.USER.name());
    }

    /**
     * 设置当前用户 ID 和角色。
     *
     * @param userId 用户 ID
     * @param role 用户角色
     */
    public static void setCurrentUser(Long userId, String role) {
        CURRENT_USER.set(userId);
        CURRENT_ROLE.set(UserRole.from(role).name());
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 当前用户 ID
     */
    public static Long getCurrentUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new IllegalStateException("当前请求未登录");
        }
        return userId;
    }

    /**
     * 获取当前用户角色。
     *
     * @return 当前用户角色
     */
    public static String getCurrentRole() {
        String role = CURRENT_ROLE.get();
        if (role == null) {
            throw new IllegalStateException("当前请求未登录");
        }
        return role;
    }

    /**
     * 清理当前线程用户上下文。
     */
    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_ROLE.remove();
    }
}
