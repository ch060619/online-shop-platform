package com.example.shop.service.impl;

import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.AddCartItemRequest;
import com.example.shop.domain.dto.UpdateCartItemRequest;
import com.example.shop.domain.entity.CartItem;
import com.example.shop.domain.entity.CartItemDetail;
import com.example.shop.domain.entity.Product;
import com.example.shop.domain.vo.CartItemVO;
import com.example.shop.domain.vo.CartVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.CartItemMapper;
import com.example.shop.repository.mapper.ProductMapper;
import com.example.shop.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 购物车服务实现。
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    /**
     * 创建购物车服务实现。
     *
     * @param cartItemMapper 购物车 Mapper
     * @param productMapper 商品 Mapper
     */
    public CartServiceImpl(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    /**
     * 查询当前用户购物车。
     *
     * @return 购物车信息
     */
    @Override
    public CartVO getCurrentCart() {
        return buildCart(cartItemMapper.findDetailsByUserId(UserContext.getCurrentUserId()));
    }

    /**
     * 加入购物车。
     *
     * @param request 加购请求
     * @return 最新购物车信息
     */
    @Override
    public CartVO addItem(AddCartItemRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Product product = requireProduct(request.getProductId());
        CartItem existing = cartItemMapper.findByUserIdAndProductId(userId, request.getProductId());
        int targetQuantity = request.getQuantity();
        if (existing != null) {
            targetQuantity = existing.getQuantity() + request.getQuantity();
        }
        ensureStock(product, targetQuantity);
        if (existing == null) {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItemMapper.insert(cartItem);
            return getCurrentCart();
        }
        cartItemMapper.updateQuantity(existing.getId(), userId, targetQuantity);
        return getCurrentCart();
    }

    /**
     * 修改购物车明细数量。
     *
     * @param itemId 购物车明细 ID
     * @param request 修改数量请求
     * @return 最新购物车信息
     */
    @Override
    public CartVO updateItem(Long itemId, UpdateCartItemRequest request) {
        Long userId = UserContext.getCurrentUserId();
        CartItem item = cartItemMapper.findByIdAndUserId(itemId, userId);
        if (item == null) {
            throw new BusinessException(404, "购物车商品不存在");
        }
        Product product = requireProduct(item.getProductId());
        ensureStock(product, request.getQuantity());
        cartItemMapper.updateQuantity(itemId, userId, request.getQuantity());
        return getCurrentCart();
    }

    /**
     * 删除购物车明细。
     *
     * @param itemId 购物车明细 ID
     * @return 最新购物车信息
     */
    @Override
    public CartVO deleteItem(Long itemId) {
        int affected = cartItemMapper.deleteByIdAndUserId(itemId, UserContext.getCurrentUserId());
        if (affected == 0) {
            throw new BusinessException(404, "购物车商品不存在");
        }
        return getCurrentCart();
    }

    private Product requireProduct(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    private void ensureStock(Product product, Integer quantity) {
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足：" + product.getName());
        }
    }

    private CartVO buildCart(List<CartItemDetail> details) {
        CartVO cart = new CartVO();
        for (CartItemDetail detail : details) {
            CartItemVO itemVO = toCartItemVO(detail);
            cart.getItems().add(itemVO);
            cart.setTotalQuantity(cart.getTotalQuantity() + itemVO.getQuantity());
            cart.setTotalAmount(cart.getTotalAmount().add(itemVO.getSubtotal()));
        }
        return cart;
    }

    private CartItemVO toCartItemVO(CartItemDetail detail) {
        CartItemVO vo = new CartItemVO();
        vo.setId(detail.getId());
        vo.setProductId(detail.getProductId());
        vo.setProductName(detail.getProductName());
        vo.setCategory(detail.getCategory());
        vo.setPrice(detail.getPrice());
        vo.setQuantity(detail.getQuantity());
        vo.setStock(detail.getStock());
        vo.setImageUrl(detail.getImageUrl());
        vo.setSubtotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        return vo;
    }
}
