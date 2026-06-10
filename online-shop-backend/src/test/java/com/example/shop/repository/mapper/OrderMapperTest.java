package com.example.shop.repository.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.common.OrderStatus;
import com.example.shop.domain.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * OrderMapper 查询测试。
 */
@MybatisTest
@ActiveProfiles("sqlite")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_returnCreatedAt_when_orderInsertedWithLocalDateTime() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 10, 21, 30, 15);
        Order order = order("TEST_TIME_1", createdAt);

        int inserted = orderMapper.insert(order);
        Order found = orderMapper.findByIdAndUserId(order.getId(), 1L);
        String rawCreatedAt = jdbcTemplate.queryForObject(
                "SELECT created_at FROM orders WHERE id = ?",
                String.class,
                order.getId());

        assertThat(inserted).isEqualTo(1);
        assertThat(found.getCreatedAt()).isEqualTo(createdAt);
        assertThat(rawCreatedAt).isEqualTo("2026-06-10 21:30:15");
    }

    @Test
    void should_returnCreatedAt_when_existingValueUsesIsoSeparator() {
        jdbcTemplate.update("INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, "
                        + "receiver_phone, receiver_address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "TEST_TIME_2",
                1L,
                new BigDecimal("20.00"),
                OrderStatus.CREATED.name(),
                "张三",
                "13800000000",
                "上海市",
                "2026-06-10T21:30:15");
        Long orderId = jdbcTemplate.queryForObject(
                "SELECT id FROM orders WHERE order_no = ?",
                Long.class,
                "TEST_TIME_2");

        Order found = orderMapper.findByIdAndUserId(orderId, 1L);

        assertThat(found.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 10, 21, 30, 15));
    }

    private Order order(String orderNo, LocalDateTime createdAt) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.CREATED.name());
        order.setReceiverName("张三");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("上海市");
        order.setCreatedAt(createdAt);
        return order;
    }
}
