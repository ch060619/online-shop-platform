package com.example.shop.service.idempotency;

/**
 * 订单幂等处理动作。
 */
public enum OrderIdempotencyAction {

    /**
     * 首次请求，允许进入业务事务。
     */
    PROCEED,

    /**
     * 请求已成功处理，返回已有订单。
     */
    REPLAY,

    /**
     * 相同请求仍在处理中。
     */
    PROCESSING,

    /**
     * 相同幂等键对应不同请求体。
     */
    CONFLICT
}
