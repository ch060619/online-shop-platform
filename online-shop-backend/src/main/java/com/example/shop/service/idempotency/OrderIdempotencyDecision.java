package com.example.shop.service.idempotency;

import java.util.Optional;

/**
 * 订单幂等处理决策。
 *
 * @param action 幂等处理动作
 * @param orderId 已成功创建的订单 ID
 */
public record OrderIdempotencyDecision(OrderIdempotencyAction action, Optional<Long> orderId) {

    /**
     * 创建首次处理决策。
     *
     * @return 首次处理决策
     */
    public static OrderIdempotencyDecision proceed() {
        return new OrderIdempotencyDecision(OrderIdempotencyAction.PROCEED, Optional.empty());
    }

    /**
     * 创建成功重放决策。
     *
     * @param orderId 已创建订单 ID
     * @return 成功重放决策
     */
    public static OrderIdempotencyDecision replay(Long orderId) {
        return new OrderIdempotencyDecision(OrderIdempotencyAction.REPLAY, Optional.of(orderId));
    }

    /**
     * 创建处理中决策。
     *
     * @return 处理中决策
     */
    public static OrderIdempotencyDecision processing() {
        return new OrderIdempotencyDecision(OrderIdempotencyAction.PROCESSING, Optional.empty());
    }

    /**
     * 创建冲突决策。
     *
     * @return 冲突决策
     */
    public static OrderIdempotencyDecision conflict() {
        return new OrderIdempotencyDecision(OrderIdempotencyAction.CONFLICT, Optional.empty());
    }
}
