package com.example.shop.service;

import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.service.idempotency.OrderIdempotencyDecision;

/**
 * 订单幂等服务接口。
 */
public interface OrderIdempotencyService {

    /**
     * 根据创建订单请求生成摘要。
     *
     * @param request 创建订单请求
     * @return 请求摘要
     */
    String fingerprint(CreateOrderRequest request);

    /**
     * 开始处理幂等请求。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     * @return 幂等处理决策
     */
    OrderIdempotencyDecision begin(Long userId, String idempotencyKey, String requestHash);

    /**
     * 记录订单创建成功结果。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     * @param orderId 订单 ID
     */
    void markSuccess(Long userId, String idempotencyKey, String requestHash, Long orderId);

    /**
     * 清理仍处于处理中的幂等状态。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     */
    void clearProcessing(Long userId, String idempotencyKey, String requestHash);
}
