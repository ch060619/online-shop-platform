package com.example.shop.service.impl;

import com.example.shop.common.OrderStatus;
import com.example.shop.common.UserContext;
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
import com.example.shop.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务实现。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * 创建订单服务实现。
     *
     * @param cartItemMapper 购物车 Mapper
     * @param productMapper 商品 Mapper
     * @param orderMapper 订单 Mapper
     * @param orderItemMapper 订单明细 Mapper
     */
    public OrderServiceImpl(CartItemMapper cartItemMapper,
                            ProductMapper productMapper,
                            OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
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
        Long userId = UserContext.getCurrentUserId();
        List<CartItemDetail> cartItems = cartItemMapper.findDetailsByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空，不能提交订单");
        }
        BigDecimal totalAmount = calculateTotalAmount(cartItems);
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED.name());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
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
        cartItemMapper.deleteByUserId(userId);
        return getOrder(order.getId());
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
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许取消");
        }
        List<OrderItemDetail> items = orderItemMapper.findByOrderId(order.getId());
        for (OrderItemDetail item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
        orderMapper.updateStatus(orderId, UserContext.getCurrentUserId(), OrderStatus.CANCELLED.name());
        return getOrder(orderId);
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

    private String generateOrderNo() {
        String timePart = LocalDateTime.now().format(ORDER_NO_TIME_FORMAT);
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

    private OrderSummaryVO toOrderSummaryVO(Order order) {
        OrderSummaryVO vo = new OrderSummaryVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
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
        vo.setItems(items.stream().map(this::toOrderItemVO).collect(Collectors.toList()));
        return vo;
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
