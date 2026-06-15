package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.dto.UpdateOrderRequest;
import com.example.shop.domain.vo.OrderSummaryVO;
import com.example.shop.domain.vo.OrderVO;
import com.example.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单 REST 控制器。
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理", description = "订单提交、列表、详情和取消接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单控制器。
     *
     * @param orderService 订单服务
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 提交订单。
     *
     * @param request 创建订单请求
     * @param idempotencyKey 幂等键
     * @return 订单详情响应
     */
    @PostMapping
    @Operation(summary = "提交订单", description = "根据当前用户购物车创建订单")
    public ApiResponse<OrderVO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.success("提交订单成功", orderService.createOrder(request, idempotencyKey));
    }

    /**
     * 查询订单列表。
     *
     * @return 订单摘要列表响应
     */
    @GetMapping
    @Operation(summary = "查询订单列表", description = "查询当前用户订单列表")
    public ApiResponse<List<OrderSummaryVO>> listOrders() {
        return ApiResponse.success(orderService.listOrders());
    }

    /**
     * 查询订单详情。
     *
     * @param id 订单 ID
     * @return 订单详情响应
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询订单详情", description = "根据订单 ID 查询当前用户订单详情")
    public ApiResponse<OrderVO> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    /**
     * 更新订单收货信息。
     *
     * @param id 订单 ID
     * @param request 更新订单请求
     * @return 更新后的订单详情响应
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新订单", description = "更新当前用户已创建订单的收货信息")
    public ApiResponse<OrderVO> updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {
        return ApiResponse.success("更新订单成功", orderService.updateOrder(id, request));
    }

    /**
     * 取消订单。
     *
     * @param id 订单 ID
     * @return 订单详情响应
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消已创建订单并回补库存")
    public ApiResponse<OrderVO> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success("取消订单成功", orderService.cancelOrder(id));
    }

    /**
     * 删除订单。
     *
     * @param id 订单 ID
     * @return 删除结果响应
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "删除当前用户已取消的订单")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("删除订单成功", (Void) null);
    }
}
