package com.example.shop.service;

import com.example.shop.domain.dto.AddCartItemRequest;
import com.example.shop.domain.dto.UpdateCartItemRequest;
import com.example.shop.domain.vo.CartVO;

/**
 * 购物车服务接口。
 */
public interface CartService {

    /**
     * 查询当前用户购物车。
     *
     * @return 购物车信息
     */
    CartVO getCurrentCart();

    /**
     * 加入购物车。
     *
     * @param request 加购请求
     * @return 最新购物车信息
     */
    CartVO addItem(AddCartItemRequest request);

    /**
     * 修改购物车明细数量。
     *
     * @param itemId 购物车明细 ID
     * @param request 修改数量请求
     * @return 最新购物车信息
     */
    CartVO updateItem(Long itemId, UpdateCartItemRequest request);

    /**
     * 删除购物车明细。
     *
     * @param itemId 购物车明细 ID
     * @return 最新购物车信息
     */
    CartVO deleteItem(Long itemId);
}
