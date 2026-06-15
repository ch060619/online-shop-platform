package com.example.shop.service.impl;

import com.example.shop.config.OrderStateMachineProperties;
import com.example.shop.service.OrderTimeoutMessagePublisher;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ 订单超时消息发布器。
 */
@Service
public class RabbitOrderTimeoutMessagePublisher implements OrderTimeoutMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderStateMachineProperties properties;

    /**
     * 创建 RabbitMQ 订单超时消息发布器。
     *
     * @param rabbitTemplate RabbitMQ 模板
     * @param properties 订单状态机配置
     */
    public RabbitOrderTimeoutMessagePublisher(RabbitTemplate rabbitTemplate,
                                              OrderStateMachineProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    /**
     * 发布订单超时延迟消息。
     *
     * @param orderId 订单 ID
     * @param expireAt 订单支付截止时间
     */
    @Override
    public void publishTimeoutMessage(Long orderId, LocalDateTime expireAt) {
        long delayMillis = Math.max(0L, Duration.between(LocalDateTime.now(), expireAt).toMillis());
        rabbitTemplate.convertAndSend(
                properties.getRabbitTimeoutExchange(),
                properties.getRabbitTimeoutDelayRoutingKey(),
                String.valueOf(orderId),
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
                    return message;
                });
    }
}
