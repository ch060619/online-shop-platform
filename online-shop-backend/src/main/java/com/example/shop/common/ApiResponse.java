package com.example.shop.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * REST API 统一响应对象。
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private PageMeta page;

    /**
     * 创建成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, null);
    }

    /**
     * 创建成功响应。
     *
     * @param message 提示信息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, null);
    }

    /**
     * 创建带分页信息的成功响应。
     *
     * @param data 响应数据
     * @param page 分页信息
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> successWithPage(T data, PageMeta page) {
        return new ApiResponse<>(200, "success", data, page);
    }

    /**
     * 创建错误响应。
     *
     * @param code 状态码
     * @param message 错误提示
     * @param <T> 响应数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }

    /**
     * 创建带错误数据的错误响应。
     *
     * @param code 状态码
     * @param message 错误提示
     * @param data 错误数据
     * @param <T> 响应数据类型
     * @return 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, null);
    }
}
