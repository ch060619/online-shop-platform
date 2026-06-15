package com.example.shop.config;

import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单超时 RabbitMQ 队列配置。
 */
@Configuration
public class OrderRabbitConfig {

    private static final String DEAD_LETTER_EXCHANGE_ARGUMENT = "x-dead-letter-exchange";
    private static final String DEAD_LETTER_ROUTING_KEY_ARGUMENT = "x-dead-letter-routing-key";
    private final OrderStateMachineProperties properties;

    /**
     * 创建订单 RabbitMQ 配置。
     *
     * @param properties 订单状态机配置
     */
    public OrderRabbitConfig(OrderStateMachineProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建订单超时交换机。
     *
     * @return 订单超时交换机
     */
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return new DirectExchange(properties.getRabbitTimeoutExchange(), true, false);
    }

    /**
     * 创建订单超时延迟队列。
     *
     * @return 订单超时延迟队列
     */
    @Bean
    public Queue orderTimeoutDelayQueue() {
        return QueueBuilder.durable(properties.getRabbitTimeoutDelayQueue())
                .withArguments(Map.of(
                        DEAD_LETTER_EXCHANGE_ARGUMENT, properties.getRabbitTimeoutExchange(),
                        DEAD_LETTER_ROUTING_KEY_ARGUMENT, properties.getRabbitTimeoutRoutingKey()))
                .build();
    }

    /**
     * 创建订单超时消费队列。
     *
     * @return 订单超时消费队列
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(properties.getRabbitTimeoutQueue()).build();
    }

    /**
     * 绑定订单超时延迟队列。
     *
     * @param orderTimeoutExchange 订单超时交换机
     * @param orderTimeoutDelayQueue 订单超时延迟队列
     * @return 队列绑定
     */
    @Bean
    public Binding orderTimeoutDelayBinding(
            DirectExchange orderTimeoutExchange,
            @Qualifier("orderTimeoutDelayQueue") Queue orderTimeoutDelayQueue) {
        return BindingBuilder.bind(orderTimeoutDelayQueue)
                .to(orderTimeoutExchange)
                .with(properties.getRabbitTimeoutDelayRoutingKey());
    }

    /**
     * 绑定订单超时消费队列。
     *
     * @param orderTimeoutExchange 订单超时交换机
     * @param orderTimeoutQueue 订单超时消费队列
     * @return 队列绑定
     */
    @Bean
    public Binding orderTimeoutBinding(
            DirectExchange orderTimeoutExchange,
            @Qualifier("orderTimeoutQueue") Queue orderTimeoutQueue) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderTimeoutExchange)
                .with(properties.getRabbitTimeoutRoutingKey());
    }
}
