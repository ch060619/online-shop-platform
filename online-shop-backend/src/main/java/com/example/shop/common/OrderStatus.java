package com.example.shop.common;

/**
 * 订单状态枚举。
 */
public enum OrderStatus {

    /**
     * 已创建。
     */
    CREATED,

    /**
     * 已支付。
     */
    PAID,

    /**
     * 已取消。
     */
    CANCELLED,

    /**
     * 已超时。
     */
    TIMEOUT
}
