package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.common.OrderStatus;
import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.entity.CartItemDetail;
import com.example.shop.domain.entity.Order;
import com.example.shop.domain.entity.OrderItemDetail;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.CartItemMapper;
import com.example.shop.repository.mapper.OrderItemMapper;
import com.example.shop.repository.mapper.OrderMapper;
import com.example.shop.repository.mapper.ProductMapper;
import com.example.shop.service.OrderIdempotencyService;
import com.example.shop.service.idempotency.OrderIdempotencyDecision;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 订单幂等并发测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderIdempotencyConcurrencyTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private OrderIdempotencyService orderIdempotencyService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);
        orderService = new OrderServiceImpl(
                cartItemMapper, productMapper, orderMapper, orderItemMapper, orderIdempotencyService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_createSingleOrder_when_sameKeySubmittedConcurrently() throws Exception {
        AtomicBoolean firstRequest = new AtomicBoolean(true);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "same-key", "hash-1")).thenAnswer(invocation -> {
            if (firstRequest.getAndSet(false)) {
                return OrderIdempotencyDecision.proceed();
            }
            return OrderIdempotencyDecision.replay(10L);
        });
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(cartDetail()));
        when(productMapper.decreaseStock(1L, 2)).thenReturn(1);
        when(orderMapper.insert(orderCaptor.capture())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return 1;
        });
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order());
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        List<Long> orderIds = runConcurrentCreates(20);

        assertThat(orderIds).containsOnly(10L);
        verify(orderMapper, atMostOnce()).insert(any(Order.class));
        verify(productMapper, atMostOnce()).decreaseStock(1L, 2);
    }

    private List<Long> runConcurrentCreates(int count) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Long>> tasks = java.util.stream.IntStream.range(0, count)
                    .mapToObj(index -> (Callable<Long>) () -> {
                        startLatch.await(3, TimeUnit.SECONDS);
                        UserContext.setCurrentUserId(1L);
                        try {
                            return orderService.createOrder(createRequest(), "same-key").getId();
                        }
                        catch (BusinessException exception) {
                            return null;
                        }
                        finally {
                            UserContext.clear();
                        }
                    })
                    .toList();
            var futures = tasks.stream().map(executor::submit).toList();
            startLatch.countDown();
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get(3, TimeUnit.SECONDS);
                        }
                        catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        finally {
            executor.shutdownNow();
        }
    }

    private CreateOrderRequest createRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("张三");
        request.setReceiverPhone("13800000000");
        request.setReceiverAddress("上海市");
        return request;
    }

    private CartItemDetail cartDetail() {
        CartItemDetail detail = new CartItemDetail();
        detail.setProductId(1L);
        detail.setProductName("商品");
        detail.setPrice(new BigDecimal("10.00"));
        detail.setQuantity(2);
        detail.setStock(5);
        detail.setImageUrl("image");
        return detail;
    }

    private Order order() {
        Order order = new Order();
        order.setId(10L);
        order.setOrderNo("NO1");
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.CREATED.name());
        order.setReceiverName("张三");
        order.setReceiverPhone("13800000000");
        order.setReceiverAddress("上海市");
        return order;
    }

    private OrderItemDetail orderItemDetail() {
        OrderItemDetail detail = new OrderItemDetail();
        detail.setId(1L);
        detail.setOrderId(10L);
        detail.setProductId(1L);
        detail.setProductName("商品");
        detail.setProductImageUrl("image");
        detail.setPrice(new BigDecimal("10.00"));
        detail.setQuantity(2);
        detail.setSubtotal(new BigDecimal("20.00"));
        return detail;
    }
}
