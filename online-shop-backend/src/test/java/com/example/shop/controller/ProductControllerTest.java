package com.example.shop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.dto.ProductSaveRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.service.ProductService;
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
 * ProductController 切片测试。
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnProducts_when_requestListApi() throws Exception {
        when(productService.search(ArgumentMatchers.any(ProductSearchRequest.class)))
                .thenReturn(PageVO.of(Collections.singletonList(product()), 1, 1, 6));

        mockMvc.perform(get("/api/products")
                        .param("name", "键盘")
                        .param("category", "数码配件")
                        .param("page", "1")
                        .param("pageSize", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].name").value("机械键盘"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(6))
                .andExpect(jsonPath("$.page.total").value(1));
    }

    @Test
    void should_returnProduct_when_requestDetailApi() throws Exception {
        when(productService.getById(1L)).thenReturn(product());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void should_returnProducts_when_requestQueryApi() throws Exception {
        when(productService.search(ArgumentMatchers.any(ProductSearchRequest.class)))
                .thenReturn(PageVO.of(Collections.singletonList(product()), 1, 1, 6));

        mockMvc.perform(get("/api/products/query").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.page.pageSize").value(6));
    }

    @Test
    void should_returnProduct_when_addProductValid() throws Exception {
        when(productService.add(ArgumentMatchers.any(ProductSaveRequest.class))).thenReturn(product());

        mockMvc.perform(post("/api/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("新增商品成功"))
                .andExpect(jsonPath("$.data.name").value("机械键盘"));
    }

    @Test
    void should_returnProduct_when_updateProductValid() throws Exception {
        when(productService.update(ArgumentMatchers.eq(1L), ArgumentMatchers.any(ProductSaveRequest.class)))
                .thenReturn(product());

        mockMvc.perform(put("/api/products/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("更新商品成功"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void should_returnSuccess_when_deleteProductValid() throws Exception {
        mockMvc.perform(delete("/api/products/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除商品成功"));
    }

    @Test
    void should_return400_when_addProductInvalid() throws Exception {
        mockMvc.perform(post("/api/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"category\":\"数码配件\",\"price\":0,\"stock\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.name").value("商品名称不能为空"));
    }

    @Test
    void should_return400_when_pathVariableTypeInvalid() throws Exception {
        mockMvc.perform(get("/api/products/not-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void should_return404_when_productNotFound() throws Exception {
        when(productService.getById(99L)).thenThrow(new BusinessException(404, "商品不存在"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("商品不存在"));
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

    private String productJson() {
        return "{\"name\":\"机械键盘\",\"category\":\"数码配件\",\"price\":299.00,"
                + "\"stock\":10,\"imageUrl\":\"image\",\"description\":\"desc\"}";
    }
}
