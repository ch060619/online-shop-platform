package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.entity.Product;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.ProductMapper;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProductServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void should_returnProducts_when_searchConditionValid() {
        Product product = product();
        when(productMapper.count("键盘", null, null, null)).thenReturn(1L);
        when(productMapper.search("键盘", null, null, null, 6, 0)).thenReturn(Collections.singletonList(product));
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("键盘");

        var page = productService.search(request);

        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getName()).isEqualTo("机械键盘");
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getPageSize()).isEqualTo(6);
    }

    @Test
    void should_querySecondPage_when_pageParamsProvided() {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCategory("数码配件");
        request.setPage(2);
        request.setPageSize(2);
        when(productMapper.count(null, "数码配件", null, null)).thenReturn(3L);
        when(productMapper.search(null, "数码配件", null, null, 2, 2)).thenReturn(Collections.emptyList());

        var page = productService.search(request);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        verify(productMapper).search(null, "数码配件", null, null, 2, 2);
    }

    @Test
    void should_throwException_when_minPriceGreaterThanMaxPrice() {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMinPrice(new BigDecimal("100.00"));
        request.setMaxPrice(new BigDecimal("50.00"));

        assertThatThrownBy(() -> productService.search(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最低价格不能大于最高价格");
    }

    @Test
    void should_returnProduct_when_productExists() {
        when(productMapper.findById(1L)).thenReturn(product());

        assertThat(productService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    void should_throwException_when_productNotFound() {
        when(productMapper.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品不存在");
    }

    private Product product() {
        Product product = new Product();
        product.setId(1L);
        product.setName("机械键盘");
        product.setCategory("数码配件");
        product.setPrice(new BigDecimal("299.00"));
        product.setStock(10);
        product.setImageUrl("image");
        product.setDescription("desc");
        return product;
    }
}
