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
    private boolean timeoutScanEnabled;
    private long timeoutScanFixedDelayMillis = 60_000;
    private int timeoutScanBatchSize = 100;
    private String rabbitTimeoutExchange = "shop.order.timeout.exchange";
    private String rabbitTimeoutDelayQueue = "shop.order.timeout.delay.queue";
    private String rabbitTimeoutQueue = "shop.order.timeout.queue";
    private String rabbitTimeoutDelayRoutingKey = "shop.order.timeout.delay";
    private String rabbitTimeoutRoutingKey = "shop.order.timeout";
}
