package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.AddCartItemRequest;
import com.example.shop.domain.dto.UpdateCartItemRequest;
import com.example.shop.domain.entity.CartItem;
import com.example.shop.domain.entity.CartItemDetail;
import com.example.shop.domain.entity.Product;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.CartItemMapper;
import com.example.shop.repository.mapper.ProductMapper;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CartServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_returnCart_when_cartHasItems() {
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(detail()));

        assertThat(cartService.getCurrentCart().getTotalQuantity()).isEqualTo(2);
        assertThat(cartService.getCurrentCart().getTotalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void should_addItem_when_productStockEnough() {
        AddCartItemRequest request = addRequest(1L, 2);
        when(productMapper.findById(1L)).thenReturn(product(5));
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.emptyList());

        assertThat(cartService.addItem(request).getItems()).isEmpty();
    }

    @Test
    void should_throwException_when_addQuantityExceedsStock() {
        AddCartItemRequest request = addRequest(1L, 6);
        when(productMapper.findById(1L)).thenReturn(product(5));

        assertThatThrownBy(() -> cartService.addItem(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品库存不足");
    }

    @Test
    void should_updateItem_when_itemExists() {
        CartItem item = new CartItem();
        item.setId(8L);
        item.setProductId(1L);
        item.setQuantity(1);
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(2);
        when(cartItemMapper.findByIdAndUserId(8L, 1L)).thenReturn(item);
        when(productMapper.findById(1L)).thenReturn(product(5));
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.singletonList(detail()));

        assertThat(cartService.updateItem(8L, request).getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void should_deleteItem_when_itemExists() {
        when(cartItemMapper.deleteByIdAndUserId(8L, 1L)).thenReturn(1);
        when(cartItemMapper.findDetailsByUserId(1L)).thenReturn(Collections.emptyList());

        assertThat(cartService.deleteItem(8L).getItems()).isEmpty();
    }

    @Test
    void should_throwException_when_deleteItemNotFound() {
        when(cartItemMapper.deleteByIdAndUserId(8L, 1L)).thenReturn(0);

        assertThatThrownBy(() -> cartService.deleteItem(8L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("购物车商品不存在");
    }

    private AddCartItemRequest addRequest(Long productId, int quantity) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private Product product(int stock) {
        Product product = new Product();
        product.setId(1L);
        product.setName("商品");
        product.setStock(stock);
        return product;
    }

    private CartItemDetail detail() {
        CartItemDetail detail = new CartItemDetail();
        detail.setId(1L);
        detail.setProductId(1L);
        detail.setProductName("商品");
        detail.setCategory("分类");
        detail.setPrice(new BigDecimal("10.00"));
        detail.setQuantity(2);
        detail.setStock(5);
        detail.setImageUrl("image");
        return detail;
    }
}
