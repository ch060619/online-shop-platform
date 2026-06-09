package com.example.shop.exception;

/**
 * 业务异常。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 创建业务异常。
     *
     * @param message 错误提示
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 创建业务异常。
     *
     * @param code 业务状态码
     * @param message 错误提示
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }
}
