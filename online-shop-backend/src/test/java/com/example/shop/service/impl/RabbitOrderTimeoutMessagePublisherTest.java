package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.shop.config.OrderStateMachineProperties;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitOrderTimeoutMessagePublisher 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RabbitOrderTimeoutMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void should_publishDelayedMessage_when_orderCreated() throws Exception {
        OrderStateMachineProperties properties = new OrderStateMachineProperties();
        RabbitOrderTimeoutMessagePublisher publisher =
                new RabbitOrderTimeoutMessagePublisher(rabbitTemplate, properties);
        ArgumentCaptor<MessagePostProcessor> processorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);

        publisher.publishTimeoutMessage(10L, LocalDateTime.now().plusMinutes(30));

        verify(rabbitTemplate).convertAndSend(
                eq(properties.getRabbitTimeoutExchange()),
                eq(properties.getRabbitTimeoutDelayRoutingKey()),
                eq("10"),
                processorCaptor.capture());
        MessageProperties messageProperties = new MessageProperties();
        Message processed = processorCaptor.getValue().postProcessMessage(new Message(new byte[0], messageProperties));
        assertThat(Long.parseLong(processed.getMessageProperties().getExpiration())).isPositive();
        assertThat(processed.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
    }
}
