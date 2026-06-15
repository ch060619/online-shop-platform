package com.example.shop.service.impl;

import com.example.shop.config.OrderStateMachineProperties;
import com.example.shop.service.OrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时订单定时扫描器。
 */
@Component
@ConditionalOnProperty(prefix = "shop.order", name = "timeout-scan-enabled", havingValue = "true")
public class OrderTimeoutScheduler {

    private final OrderService orderService;
    private final OrderStateMachineProperties properties;

    /**
     * 创建超时订单定时扫描器。
     *
     * @param orderService 订单服务
     * @param properties 订单状态机配置
     */
    public OrderTimeoutScheduler(OrderService orderService, OrderStateMachineProperties properties) {
        this.orderService = orderService;
        this.properties = properties;
    }

    /**
     * 定时处理超时未支付订单。
     */
    @Scheduled(fixedDelayString = "${shop.order.timeout-scan-fixed-delay-millis:60000}")
    public void scanTimeoutOrders() {
        orderService.timeoutExpiredOrders(properties.getTimeoutScanBatchSize());
    }
}
