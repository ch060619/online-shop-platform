package com.example.shop.service.impl;

import com.example.shop.common.OrderStatus;
import com.example.shop.common.UserContext;
import com.example.shop.config.OrderStateMachineProperties;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.dto.UpdateOrderRequest;
import com.example.shop.domain.entity.CartItemDetail;
import com.example.shop.domain.entity.Order;
import com.example.shop.domain.entity.OrderItem;
import com.example.shop.domain.entity.OrderItemDetail;
import com.example.shop.domain.vo.OrderItemVO;
import com.example.shop.domain.vo.OrderSummaryVO;
import com.example.shop.domain.vo.OrderVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.CartItemMapper;
import com.example.shop.repository.mapper.OrderItemMapper;
import com.example.shop.repository.mapper.OrderMapper;
import com.example.shop.repository.mapper.ProductMapper;
import com.example.shop.service.OrderIdempotencyService;
import com.example.shop.service.OrderService;
import com.example.shop.service.OrderTimeoutMessagePublisher;
import com.example.shop.service.idempotency.OrderIdempotencyAction;
import com.example.shop.service.idempotency.OrderIdempotencyDecision;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 订单服务实现。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderIdempotencyService orderIdempotencyService;
    private final OrderStateMachineProperties stateMachineProperties;
    private final OrderTimeoutMessagePublisher orderTimeoutMessagePublisher;

    /**
     * 创建订单服务实现。
     *
     * @param cartItemMapper 购物车 Mapper
     * @param productMapper 商品 Mapper
     * @param orderMapper 订单 Mapper
     * @param orderItemMapper 订单明细 Mapper
     * @param orderIdempotencyService 订单幂等服务
     * @param stateMachineProperties 订单状态机配置
     * @param orderTimeoutMessagePublisher 订单超时消息发布器
     */
    public OrderServiceImpl(CartItemMapper cartItemMapper,
                            ProductMapper productMapper,
                            OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            OrderIdempotencyService orderIdempotencyService,
                            OrderStateMachineProperties stateMachineProperties,
                            OrderTimeoutMessagePublisher orderTimeoutMessagePublisher) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderIdempotencyService = orderIdempotencyService;
        this.stateMachineProperties = stateMachineProperties;
        this.orderTimeoutMessagePublisher = orderTimeoutMessagePublisher;
    }

    /**
     * 根据当前用户购物车创建订单。
     *
     * @param request 创建订单请求
     * @return 订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request) {
        return createOrderWithoutIdempotency(request);
    }

    /**
     * 根据当前用户购物车幂等创建订单。
     *
     * @param request 创建订单请求
     * @param idempotencyKey 幂等键
     * @return 订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BusinessException("Idempotency-Key 不能为空");
        }
        Long userId = UserContext.getCurrentUserId();
        String trimmedKey = idempotencyKey.trim();
        String requestHash = orderIdempotencyService.fingerprint(request);
        OrderIdempotencyDecision decision = orderIdempotencyService.begin(userId, trimmedKey, requestHash);
        if (decision.action() == OrderIdempotencyAction.REPLAY) {
            return getOrder(decision.orderId().orElseThrow());
        }
        if (decision.action() == OrderIdempotencyAction.PROCESSING) {
            throw new BusinessException(409, "订单正在处理中，请勿重复提交");
        }
        if (decision.action() == OrderIdempotencyAction.CONFLICT) {
            throw new BusinessException(409, "Idempotency-Key 已用于不同请求");
        }
        try {
            OrderVO order = createOrderWithoutIdempotency(request);
            orderIdempotencyService.markSuccess(userId, trimmedKey, requestHash, order.getId());
            return order;
        }
        catch (RuntimeException exception) {
            orderIdempotencyService.clearProcessing(userId, trimmedKey, requestHash);
            throw exception;
        }
    }

    private OrderVO createOrderWithoutIdempotency(CreateOrderRequest request) {
        Long userId = UserContext.getCurrentUserId();
        List<CartItemDetail> cartItems = cartItemMapper.findSelectedDetailsByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("请选择要结算的购物车商品");
        }
        BigDecimal totalAmount = calculateTotalAmount(cartItems);
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo(generateOrderNo(now));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED.name());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setCreatedAt(now);
        order.setExpireAt(now.plusMinutes(stateMachineProperties.getPaymentTimeoutMinutes()));
        order.setUpdatedAt(now);
        orderMapper.insert(order);

        for (CartItemDetail cartItem : cartItems) {
            if (cartItem.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("商品库存不足：" + cartItem.getProductName());
            }
            int affected = productMapper.decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
            if (affected == 0) {
                throw new BusinessException("商品库存不足：" + cartItem.getProductName());
            }
            orderItemMapper.insert(toOrderItem(order.getId(), cartItem));
        }
        cartItemMapper.deleteSelectedByUserId(userId);
        OrderVO createdOrder = getOrder(order.getId());
        publishTimeoutMessageAfterCommit(order.getId(), order.getExpireAt());
        return createdOrder;
    }

    /**
     * 查询当前用户订单列表。
     *
     * @return 订单摘要列表
     */
    @Override
    public List<OrderSummaryVO> listOrders() {
        return orderMapper.findByUserId(UserContext.getCurrentUserId()).stream()
                .map(this::toOrderSummaryVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    @Override
    public OrderVO getOrder(Long orderId) {
        Order order = requireOrder(orderId);
        List<OrderItemDetail> items = orderItemMapper.findByOrderId(order.getId());
        return toOrderVO(order, items);
    }

    /**
     * 更新订单收货信息。
     *
     * @param orderId 订单 ID
     * @param request 更新订单请求
     * @return 更新后的订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO updateOrder(Long orderId, UpdateOrderRequest request) {
        Order order = requireOrder(orderId);
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许修改");
        }
        int affected = orderMapper.updateReceiver(orderId,
                UserContext.getCurrentUserId(),
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverAddress());
        if (affected == 0) {
            throw new BusinessException(404, "订单不存在");
        }
        return getOrder(orderId);
    }

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @return 取消后的订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancelOrder(Long orderId) {
        Order order = requireOrder(orderId);
        boolean cancelled = closeCreatedOrder(
                order.getId(), order.getUserId(), OrderStatus.CANCELLED, LocalDateTime.now(), true);
        if (!cancelled) {
            throw new BusinessException("当前订单状态不允许取消");
        }
        return getOrder(orderId);
    }

    /**
     * 支付已创建订单。
     *
     * @param orderId 订单 ID
     * @return 支付后的订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO payOrder(Long orderId) {
        Order order = requireOrder(orderId);
        LocalDateTime now = LocalDateTime.now();
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许支付");
        }
        if (order.getExpireAt() != null && !order.getExpireAt().isAfter(now)) {
            closeCreatedOrder(order.getId(), order.getUserId(), OrderStatus.TIMEOUT, now, true);
            throw new BusinessException("订单已超时");
        }
        int affected = orderMapper.updateStatusWhen(
                order.getId(),
                order.getUserId(),
                OrderStatus.CREATED.name(),
                OrderStatus.PAID.name(),
                now,
                now);
        if (affected == 0) {
            throw new BusinessException("当前订单状态不允许支付");
        }
        return getOrder(orderId);
    }

    /**
     * 处理单笔超时订单。
     *
     * @param orderId 订单 ID
     * @return 是否完成超时关闭
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean timeoutOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        LocalDateTime now = LocalDateTime.now();
        if (order == null || order.getExpireAt() == null || order.getExpireAt().isAfter(now)) {
            return false;
        }
        return closeCreatedOrder(order.getId(), order.getUserId(), OrderStatus.TIMEOUT, now, false);
    }

    /**
     * 扫描并处理已超时订单。
     *
     * @param limit 每批处理上限
     * @return 本次处理成功关闭的订单数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int timeoutExpiredOrders(int limit) {
        List<Order> orders = orderMapper.findExpiredCreatedOrders(
                LocalDateTime.now(), OrderStatus.CREATED.name(), limit);
        int closedCount = 0;
        for (Order order : orders) {
            if (timeoutOrder(order.getId())) {
                closedCount++;
            }
        }
        return closedCount;
    }

    /**
     * 删除已取消订单。
     *
     * @param orderId 订单 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        Order order = requireOrder(orderId);
        if (!OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new BusinessException("仅允许删除已取消订单");
        }
        orderItemMapper.deleteByOrderId(orderId);
        orderMapper.deleteByIdAndUserId(orderId, UserContext.getCurrentUserId());
    }

    private BigDecimal calculateTotalAmount(List<CartItemDetail> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDetail item : cartItems) {
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount;
    }

    private String generateOrderNo(LocalDateTime now) {
        String timePart = now.format(ORDER_NO_TIME_FORMAT);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return timePart + randomPart;
    }

    private OrderItem toOrderItem(Long orderId, CartItemDetail cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(cartItem.getProductName());
        orderItem.setProductImageUrl(cartItem.getImageUrl());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return orderItem;
    }

    private Order requireOrder(Long orderId) {
        Order order = orderMapper.findByIdAndUserId(orderId, UserContext.getCurrentUserId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void publishTimeoutMessageAfterCommit(Long orderId, LocalDateTime expireAt) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            safePublishTimeoutMessage(orderId, expireAt);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                safePublishTimeoutMessage(orderId, expireAt);
            }
        });
    }

    private void safePublishTimeoutMessage(Long orderId, LocalDateTime expireAt) {
        try {
            orderTimeoutMessagePublisher.publishTimeoutMessage(orderId, expireAt);
        }
        catch (RuntimeException exception) {
            log.warn("订单超时消息发布失败，后续将依赖兜底扫描处理: orderId={}, reason={}",
                    orderId, exception.getMessage());
        }
    }

    private OrderSummaryVO toOrderSummaryVO(Order order) {
        OrderSummaryVO vo = new OrderSummaryVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setExpireAt(order.getExpireAt());
        vo.setPaidAt(order.getPaidAt());
        return vo;
    }

    private OrderVO toOrderVO(Order order, List<OrderItemDetail> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setExpireAt(order.getExpireAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setItems(items.stream().map(this::toOrderItemVO).collect(Collectors.toList()));
        return vo;
    }

    private boolean closeCreatedOrder(Long orderId,
                                      Long userId,
                                      OrderStatus toStatus,
                                      LocalDateTime now,
                                      boolean userScoped) {
        int affected = userScoped
                ? orderMapper.updateStatusWhen(
                        orderId, userId, OrderStatus.CREATED.name(), toStatus.name(), null, now)
                : orderMapper.updateStatusByIdWhen(orderId, OrderStatus.CREATED.name(), toStatus.name(), now);
        if (affected == 0) {
            return false;
        }
        List<OrderItemDetail> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItemDetail item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
        return true;
    }

    private OrderItemVO toOrderItemVO(OrderItemDetail item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImageUrl(item.getProductImageUrl());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }
}
