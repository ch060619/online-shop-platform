package com.example.shop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.CreateOrderRequest;
import com.example.shop.domain.vo.OrderSummaryVO;
import com.example.shop.domain.vo.OrderVO;
import com.example.shop.service.OrderService;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OrderController 切片测试。
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnOrder_when_createOrderValid() throws Exception {
        when(tokenService.parseUserId("valid-token")).thenReturn(1L);
        when(orderService.createOrder(ArgumentMatchers.any(CreateOrderRequest.class))).thenReturn(order());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"张三\",\"receiverPhone\":\"13800000000\","
                                + "\"receiverAddress\":\"上海市\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("NO1"));
    }

    @Test
    void should_returnOrders_when_requestListApi() throws Exception {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setId(1L);
        summary.setOrderNo("NO1");
        summary.setTotalAmount(new BigDecimal("20.00"));
        when(tokenService.parseUserId("valid-token")).thenReturn(1L);
        when(orderService.listOrders()).thenReturn(Collections.singletonList(summary));

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].orderNo").value("NO1"));
    }

    @Test
    void should_return401_when_requestOrdersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    private OrderVO order() {
        OrderVO vo = new OrderVO();
        vo.setId(1L);
        vo.setOrderNo("NO1");
        vo.setTotalAmount(new BigDecimal("20.00"));
        return vo;
    }
}
