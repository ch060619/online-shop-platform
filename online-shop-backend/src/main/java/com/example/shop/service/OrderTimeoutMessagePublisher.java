package com.example.shop.service;

import java.time.LocalDateTime;

/**
 * 订单超时消息发布器。
 */
public interface OrderTimeoutMessagePublisher {

    /**
     * 发布订单超时延迟消息。
     *
     * @param orderId 订单 ID
     * @param expireAt 订单支付截止时间
     */
    void publishTimeoutMessage(Long orderId, LocalDateTime expireAt);
}
