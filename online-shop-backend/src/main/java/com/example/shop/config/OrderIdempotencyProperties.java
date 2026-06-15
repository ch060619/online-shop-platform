package com.example.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单幂等配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.idempotency.order")
public class OrderIdempotencyProperties {

    private long processingTtlSeconds = 30;
    private long successTtlSeconds = 86_400;
}
