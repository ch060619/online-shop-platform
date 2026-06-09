package com.example.shop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.AddCartItemRequest;
import com.example.shop.domain.vo.CartVO;
import com.example.shop.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CartController 切片测试。
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnCart_when_requestCartApi() throws Exception {
        CartVO cart = new CartVO();
        cart.setTotalQuantity(0);
        when(tokenService.parseUserId("valid-token")).thenReturn(1L);
        when(cartService.getCurrentCart()).thenReturn(cart);

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(0));
    }

    @Test
    void should_returnCart_when_addItemValid() throws Exception {
        when(tokenService.parseUserId("valid-token")).thenReturn(1L);
        when(cartService.addItem(ArgumentMatchers.any(AddCartItemRequest.class))).thenReturn(new CartVO());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("加入购物车成功"));
    }

    @Test
    void should_return400_when_addItemInvalid() throws Exception {
        when(tokenService.parseUserId("valid-token")).thenReturn(1L);
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void should_return401_when_requestCartWithoutToken() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
