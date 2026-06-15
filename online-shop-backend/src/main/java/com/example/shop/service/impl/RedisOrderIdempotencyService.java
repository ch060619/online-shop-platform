package com.example.shop.service.impl;

import com.example.shop.config.OrderIdempotencyProperties;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.service.OrderIdempotencyService;
import com.example.shop.service.idempotency.OrderIdempotencyDecision;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/**
 * Redis Lua 订单幂等服务实现。
 */
@Service
public class RedisOrderIdempotencyService implements OrderIdempotencyService {

    private static final String KEY_PREFIX = "shop:order:idempotency:";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String RESULT_PROCEED = "PROCEED";
    private static final String RESULT_REPLAY = "REPLAY";
    private static final String RESULT_PROCESSING = "PROCESSING";
    private static final String RESULT_CONFLICT = "CONFLICT";
    private static final DefaultRedisScript<List> BEGIN_SCRIPT = new DefaultRedisScript<>("""
            local currentHash = redis.call('HGET', KEYS[1], 'requestHash')
            if not currentHash then
                redis.call('HSET', KEYS[1], 'status', 'PROCESSING', 'requestHash', ARGV[1])
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
                return {'PROCEED'}
            end
            if currentHash ~= ARGV[1] then
                return {'CONFLICT'}
            end
            local status = redis.call('HGET', KEYS[1], 'status')
            if status == 'SUCCESS' then
                return {'REPLAY', redis.call('HGET', KEYS[1], 'orderId')}
            end
            return {'PROCESSING'}
            """, List.class);
    private static final DefaultRedisScript<Long> CLEAR_SCRIPT = new DefaultRedisScript<>("""
            local currentHash = redis.call('HGET', KEYS[1], 'requestHash')
            local status = redis.call('HGET', KEYS[1], 'status')
            if currentHash == ARGV[1] and status == 'PROCESSING' then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderIdempotencyProperties properties;

    /**
     * 创建 Redis 订单幂等服务。
     *
     * @param redisTemplate Redis 字符串模板
     * @param objectMapper JSON 序列化器
     * @param properties 订单幂等配置
     */
    public RedisOrderIdempotencyService(StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper,
                                        OrderIdempotencyProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 根据创建订单请求生成摘要。
     *
     * @param request 创建订单请求
     * @return 请求摘要
     */
    @Override
    public String fingerprint(CreateOrderRequest request) {
        try {
            return sha256(objectMapper.writeValueAsString(request));
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("无法生成订单请求摘要", exception);
        }
    }

    /**
     * 开始处理幂等请求。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     * @return 幂等处理决策
     */
    @Override
    public OrderIdempotencyDecision begin(Long userId, String idempotencyKey, String requestHash) {
        List<?> result = redisTemplate.execute(
                BEGIN_SCRIPT,
                List.of(redisKey(userId, idempotencyKey)),
                requestHash,
                String.valueOf(properties.getProcessingTtlSeconds()));
        if (result == null || result.isEmpty()) {
            throw new IllegalStateException("订单幂等状态判断失败");
        }
        return toDecision(result);
    }

    /**
     * 记录订单创建成功结果。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     * @param orderId 订单 ID
     */
    @Override
    public void markSuccess(Long userId, String idempotencyKey, String requestHash, Long orderId) {
        String key = redisKey(userId, idempotencyKey);
        redisTemplate.opsForHash().put(key, "status", STATUS_SUCCESS);
        redisTemplate.opsForHash().put(key, "requestHash", requestHash);
        redisTemplate.opsForHash().put(key, "orderId", String.valueOf(orderId));
        redisTemplate.expire(key, Duration.ofSeconds(properties.getSuccessTtlSeconds()));
    }

    /**
     * 清理仍处于处理中的幂等状态。
     *
     * @param userId 用户 ID
     * @param idempotencyKey 幂等键
     * @param requestHash 请求摘要
     */
    @Override
    public void clearProcessing(Long userId, String idempotencyKey, String requestHash) {
        redisTemplate.execute(CLEAR_SCRIPT, List.of(redisKey(userId, idempotencyKey)), requestHash);
    }

    private OrderIdempotencyDecision toDecision(List<?> result) {
        String action = String.valueOf(result.get(0));
        if (RESULT_PROCEED.equals(action)) {
            return OrderIdempotencyDecision.proceed();
        }
        if (RESULT_REPLAY.equals(action)) {
            return OrderIdempotencyDecision.replay(Long.valueOf(String.valueOf(result.get(1))));
        }
        if (RESULT_PROCESSING.equals(action)) {
            return OrderIdempotencyDecision.processing();
        }
        if (RESULT_CONFLICT.equals(action)) {
            return OrderIdempotencyDecision.conflict();
        }
        throw new IllegalStateException("未知订单幂等状态：" + action);
    }

    private String redisKey(Long userId, String idempotencyKey) {
        return KEY_PREFIX + userId + ":" + idempotencyKey;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}
