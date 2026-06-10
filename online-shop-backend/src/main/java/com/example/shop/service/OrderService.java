package com.example.shop.service;

import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.dto.UpdateOrderRequest;
import com.example.shop.domain.vo.OrderSummaryVO;
import com.example.shop.domain.vo.OrderVO;
import java.util.List;

/**
 * 订单服务接口。
 */
public interface OrderService {

    /**
     * 根据当前用户购物车创建订单。
     *
     * @param request 创建订单请求
     * @return 订单详情
     */
    OrderVO createOrder(CreateOrderRequest request);

    /**
     * 查询当前用户订单列表。
     *
     * @return 订单摘要列表
     */
    List<OrderSummaryVO> listOrders();

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    OrderVO getOrder(Long orderId);

    /**
     * 更新订单收货信息。
     *
     * @param orderId 订单 ID
     * @param request 更新订单请求
     * @return 更新后的订单详情
     */
    OrderVO updateOrder(Long orderId, UpdateOrderRequest request);

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @return 取消后的订单详情
     */
    OrderVO cancelOrder(Long orderId);

    /**
     * 删除已取消订单。
     *
     * @param orderId 订单 ID
     */
    void deleteOrder(Long orderId);
}
