package com.example.shop.service.impl;

import static org.mockito.Mockito.verify;

import com.example.shop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OrderTimeoutMessageListener 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderTimeoutMessageListenerTest {

    @Mock
    private OrderService orderService;

    @Test
    void should_delegateTimeoutOrder_when_messageReceived() {
        OrderTimeoutMessageListener listener = new OrderTimeoutMessageListener(orderService);

        listener.handleTimeoutMessage("10");

        verify(orderService).timeoutOrder(10L);
    }
}
