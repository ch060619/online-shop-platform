package com.example.shop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单状态机配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.order")
public class OrderStateMachineProperties {

    private long paymentTimeoutMinutes = 30;
    private long timeoutScanFixedDelayMillis = 60_000;
    private int timeoutScanBatchSize = 100;
}
