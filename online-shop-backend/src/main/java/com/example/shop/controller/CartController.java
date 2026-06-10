package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.AddCartItemRequest;
import com.example.shop.domain.dto.UpdateCartItemRequest;
import com.example.shop.domain.vo.CartVO;
import com.example.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车 REST 控制器。
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车管理", description = "购物车查询、加入、修改和删除接口")
public class CartController {

    private final CartService cartService;

    /**
     * 创建购物车控制器。
     *
     * @param cartService 购物车服务
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 查询购物车。
     *
     * @return 购物车响应
     */
    @GetMapping
    @Operation(summary = "查询购物车", description = "查询当前用户购物车明细和总价")
    public ApiResponse<CartVO> getCart() {
        return ApiResponse.success(cartService.getCurrentCart());
    }

    /**
     * 加入购物车。
     *
     * @param request 加购请求
     * @return 购物车响应
     */
    @PostMapping("/items")
    @Operation(summary = "加入购物车", description = "将指定商品和数量加入当前用户购物车")
    public ApiResponse<CartVO> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.success("加入购物车成功", cartService.addItem(request));
    }

    /**
     * 修改购物车数量。
     *
     * @param id 购物车明细 ID
     * @param request 修改数量请求
     * @return 购物车响应
     */
    @PutMapping("/items/{id}")
    @Operation(summary = "修改购物车数量", description = "修改购物车明细购买数量")
    public ApiResponse<CartVO> updateItem(@PathVariable Long id, @Valid @RequestBody UpdateCartItemRequest request) {
        return ApiResponse.success("修改购物车成功", cartService.updateItem(id, request));
    }

    /**
     * 删除购物车明细。
     *
     * @param id 购物车明细 ID
     * @return 购物车响应
     */
    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除购物车明细", description = "删除当前用户购物车中的指定明细")
    public ApiResponse<CartVO> deleteItem(@PathVariable Long id) {
        return ApiResponse.success("删除购物车商品成功", cartService.deleteItem(id));
    }
}
