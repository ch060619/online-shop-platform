package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.config.OrderIdempotencyProperties;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.service.idempotency.OrderIdempotencyAction;
import com.example.shop.service.idempotency.OrderIdempotencyDecision;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * RedisOrderIdempotencyService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RedisOrderIdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private RedisOrderIdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        OrderIdempotencyProperties properties = new OrderIdempotencyProperties();
        properties.setProcessingTtlSeconds(30);
        properties.setSuccessTtlSeconds(86_400);
        idempotencyService = new RedisOrderIdempotencyService(
                redisTemplate, new com.fasterxml.jackson.databind.ObjectMapper(), properties);
    }

    @Test
    void should_generateSameFingerprint_when_requestSame() {
        CreateOrderRequest request = request("张三");

        String first = idempotencyService.fingerprint(request);
        String second = idempotencyService.fingerprint(request);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
    }

    @Test
    void should_returnProceed_when_luaReturnsProceed() {
        when(redisTemplate.execute(anyListRedisScript(), anyList(), eq("hash-1"), eq("30")))
                .thenReturn(List.of("PROCEED"));

        OrderIdempotencyDecision decision = idempotencyService.begin(1L, "key-1", "hash-1");

        assertThat(decision.action()).isEqualTo(OrderIdempotencyAction.PROCEED);
    }

    @Test
    void should_returnReplay_when_luaReturnsOrderId() {
        when(redisTemplate.execute(anyListRedisScript(), anyList(), eq("hash-1"), eq("30")))
                .thenReturn(List.of("REPLAY", "10"));

        OrderIdempotencyDecision decision = idempotencyService.begin(1L, "key-1", "hash-1");

        assertThat(decision.action()).isEqualTo(OrderIdempotencyAction.REPLAY);
        assertThat(decision.orderId()).hasValue(10L);
    }

    @Test
    void should_returnConflict_when_luaReturnsConflict() {
        when(redisTemplate.execute(anyListRedisScript(), anyList(), eq("hash-1"), eq("30")))
                .thenReturn(List.of("CONFLICT"));

        OrderIdempotencyDecision decision = idempotencyService.begin(1L, "key-1", "hash-1");

        assertThat(decision.action()).isEqualTo(OrderIdempotencyAction.CONFLICT);
    }

    @Test
    void should_markSuccess_when_orderCreated() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        idempotencyService.markSuccess(1L, "key-1", "hash-1", 10L);

        verify(hashOperations).put("shop:order:idempotency:1:key-1", "status", "SUCCESS");
        verify(hashOperations).put("shop:order:idempotency:1:key-1", "requestHash", "hash-1");
        verify(hashOperations).put("shop:order:idempotency:1:key-1", "orderId", "10");
        verify(redisTemplate).expire("shop:order:idempotency:1:key-1", Duration.ofSeconds(86_400));
    }

    @Test
    void should_clearProcessing_when_requestHashMatches() {
        idempotencyService.clearProcessing(1L, "key-1", "hash-1");

        verify(redisTemplate).execute(anyLongRedisScript(), anyList(), eq("hash-1"));
    }

    private CreateOrderRequest request(String receiverName) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName(receiverName);
        request.setReceiverPhone("13800000000");
        request.setReceiverAddress("上海市");
        return request;
    }

    @SuppressWarnings("unchecked")
    private RedisScript<List> anyListRedisScript() {
        return org.mockito.ArgumentMatchers.any();
    }

    private RedisScript<Long> anyLongRedisScript() {
        return org.mockito.ArgumentMatchers.any();
    }
}
