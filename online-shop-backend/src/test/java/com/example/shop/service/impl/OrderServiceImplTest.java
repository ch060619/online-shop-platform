package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.common.OrderStatus;
import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.dto.UpdateOrderRequest;
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
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OrderServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

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

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_createOrder_when_cartStockEnough() {
        LocalDateTime beforeCreate = LocalDateTime.now();
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(cartDetail(5)));
        when(productMapper.decreaseStock(1L, 2)).thenReturn(1);
        when(orderMapper.insert(orderCaptor.capture())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return 1;
        });
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        assertThat(orderService.createOrder(createRequest()).getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(orderCaptor.getValue().getCreatedAt()).isBetween(beforeCreate, LocalDateTime.now());
        verify(cartItemMapper).deleteByUserId(1L);
    }

    @Test
    void should_createOrderOnce_when_idempotencyKeyFirstSeen() {
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "order-key-1", "hash-1"))
                .thenReturn(OrderIdempotencyDecision.proceed());
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(cartDetail(5)));
        when(productMapper.decreaseStock(1L, 2)).thenReturn(1);
        when(orderMapper.insert(orderCaptor.capture())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return 1;
        });
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        assertThat(orderService.createOrder(createRequest(), "order-key-1").getId()).isEqualTo(10L);
        verify(orderIdempotencyService).markSuccess(1L, "order-key-1", "hash-1", 10L);
        verify(productMapper).decreaseStock(1L, 2);
    }

    @Test
    void should_returnExistingOrder_when_idempotencyReplay() {
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "order-key-1", "hash-1"))
                .thenReturn(OrderIdempotencyDecision.replay(10L));
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        assertThat(orderService.createOrder(createRequest(), "order-key-1").getOrderNo()).isEqualTo("NO1");
        verify(productMapper, never()).decreaseStock(1L, 2);
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void should_throwException_when_idempotencyRequestProcessing() {
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "order-key-1", "hash-1"))
                .thenReturn(OrderIdempotencyDecision.processing());

        assertThatThrownBy(() -> orderService.createOrder(createRequest(), "order-key-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单正在处理中");
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void should_throwException_when_idempotencyKeyConflicts() {
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "order-key-1", "hash-1"))
                .thenReturn(OrderIdempotencyDecision.conflict());

        assertThatThrownBy(() -> orderService.createOrder(createRequest(), "order-key-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Idempotency-Key 已用于不同请求");
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void should_clearProcessing_when_idempotentCreateFails() {
        when(orderIdempotencyService.fingerprint(any(CreateOrderRequest.class))).thenReturn("hash-1");
        when(orderIdempotencyService.begin(1L, "order-key-1", "hash-1"))
                .thenReturn(OrderIdempotencyDecision.proceed());
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> orderService.createOrder(createRequest(), "order-key-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("购物车为空");
        verify(orderIdempotencyService).clearProcessing(1L, "order-key-1", "hash-1");
    }

    @Test
    void should_throwException_when_cartEmpty() {
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> orderService.createOrder(createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("购物车为空");
    }

    @Test
    void should_throwException_when_stockNotEnough() {
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(cartDetail(1)));
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return 1;
        });

        assertThatThrownBy(() -> orderService.createOrder(createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品库存不足");
    }

    @Test
    void should_cancelOrder_when_orderCreated() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        assertThat(orderService.cancelOrder(10L).getStatus()).isEqualTo(OrderStatus.CREATED.name());
        verify(productMapper).increaseStock(1L, 2);
        verify(orderMapper).updateStatus(10L, 1L, OrderStatus.CANCELLED.name());
    }

    @Test
    void should_updateOrder_when_orderCreated() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));
        when(orderMapper.updateReceiver(10L, 1L, "李四", "13900000000", "北京市")).thenReturn(1);
        when(orderItemMapper.findByOrderId(10L)).thenReturn(Collections.singletonList(orderItemDetail()));

        assertThat(orderService.updateOrder(10L, updateRequest()).getOrderNo()).isEqualTo("NO1");
        verify(orderMapper).updateReceiver(10L, 1L, "李四", "13900000000", "北京市");
    }

    @Test
    void should_throwException_when_cancelledOrderUpdated() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CANCELLED.name()));

        assertThatThrownBy(() -> orderService.updateOrder(10L, updateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前订单状态不允许修改");
    }

    @Test
    void should_deleteOrder_when_orderCancelled() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CANCELLED.name()));

        orderService.deleteOrder(10L);

        verify(orderItemMapper).deleteByOrderId(10L);
        verify(orderMapper).deleteByIdAndUserId(10L, 1L);
    }

    @Test
    void should_throwException_when_createdOrderDeleted() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CREATED.name()));

        assertThatThrownBy(() -> orderService.deleteOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅允许删除已取消订单");
    }

    @Test
    void should_returnOrders_when_ordersExist() {
        when(orderMapper.findByUserId(1L)).thenReturn(Collections.singletonList(order(OrderStatus.CREATED.name())));

        assertThat(orderService.listOrders()).hasSize(1);
    }

    @Test
    void should_throwException_when_orderNotFound() {
        when(orderMapper.findByIdAndUserId(99L, 1L)).thenReturn(null);

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    void should_throwException_when_cancelledOrderCancelAgain() {
        when(orderMapper.findByIdAndUserId(10L, 1L)).thenReturn(order(OrderStatus.CANCELLED.name()));

        assertThatThrownBy(() -> orderService.cancelOrder(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前订单状态不允许取消");
    }

    private CreateOrderRequest createRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("张三");
        request.setReceiverPhone("13800000000");
        request.setReceiverAddress("上海市");
        return request;
    }

    private UpdateOrderRequest updateRequest() {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("13900000000");
        request.setReceiverAddress("北京市");
        return request;
    }

    private CartItemDetail cartDetail(int stock) {
        CartItemDetail detail = new CartItemDetail();
        detail.setProductId(1L);
        detail.setProductName("商品");
        detail.setPrice(new BigDecimal("10.00"));
        detail.setQuantity(2);
        detail.setStock(stock);
        detail.setImageUrl("image");
        return detail;
    }

    private Order order(String status) {
        Order order = new Order();
        order.setId(10L);
        order.setOrderNo("NO1");
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(status);
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
