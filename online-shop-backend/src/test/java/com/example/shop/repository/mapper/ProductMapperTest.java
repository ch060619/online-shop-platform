package com.example.shop.repository.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.domain.entity.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;

/**
 * ProductMapper 查询测试。
 */
@MybatisTest
@ActiveProfiles("sqlite")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    void should_returnPagedProducts_when_categoryProvided() {
        long total = productMapper.count(null, "数码配件", null, null);

        List<Product> products = productMapper.search(null, "数码配件", null, null, 1, 1);

        assertThat(total).isEqualTo(2);
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("蓝牙降噪耳机");
    }
}
