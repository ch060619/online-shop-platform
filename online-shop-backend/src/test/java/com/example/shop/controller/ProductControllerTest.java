package com.example.shop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.ProductService;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ProductController 切片测试。
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void should_returnProducts_when_requestListApi() throws Exception {
        when(productService.search(ArgumentMatchers.any(ProductSearchRequest.class)))
                .thenReturn(Collections.singletonList(product()));

        mockMvc.perform(get("/api/products").param("name", "键盘"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("机械键盘"));
    }

    @Test
    void should_returnProduct_when_requestDetailApi() throws Exception {
        when(productService.getById(1L)).thenReturn(product());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    private ProductVO product() {
        ProductVO vo = new ProductVO();
        vo.setId(1L);
        vo.setName("机械键盘");
        vo.setCategory("数码配件");
        vo.setPrice(new BigDecimal("299.00"));
        vo.setStock(10);
        return vo;
    }
}
