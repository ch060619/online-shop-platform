package com.example.shop.service.impl;

import static org.mockito.Mockito.verify;

import com.example.shop.config.OrderStateMachineProperties;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OrderTimeoutScheduler 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderTimeoutSchedulerTest {

    @Mock
    private OrderService orderService;

    @Test
    void should_scanTimeoutOrders_when_scheduleTriggered() {
        OrderStateMachineProperties properties = new OrderStateMachineProperties();
        properties.setTimeoutScanBatchSize(50);
        OrderTimeoutScheduler scheduler = new OrderTimeoutScheduler(orderService, properties);

        scheduler.scanTimeoutOrders();

        verify(orderService).timeoutExpiredOrders(50);
    }
}
