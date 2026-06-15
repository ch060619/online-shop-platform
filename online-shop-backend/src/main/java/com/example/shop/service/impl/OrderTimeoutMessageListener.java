package com.example.shop.service.impl;

import com.example.shop.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时消息消费者。
 */
@Component
public class OrderTimeoutMessageListener {

    private final OrderService orderService;

    /**
     * 创建订单超时消息消费者。
     *
     * @param orderService 订单服务
     */
    public OrderTimeoutMessageListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 处理订单超时消息。
     *
     * @param orderId 订单 ID 文本
     */
    @RabbitListener(queues = "${shop.order.rabbit-timeout-queue:shop.order.timeout.queue}")
    public void handleTimeoutMessage(String orderId) {
        orderService.timeoutOrder(Long.valueOf(orderId));
    }
}
