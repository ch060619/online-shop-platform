package com.example.shop.repository.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.domain.entity.Product;
import java.math.BigDecimal;
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

    @Test
    void should_insertUpdateAndDeleteProduct_when_productValid() {
        Product product = product();

        int inserted = productMapper.insert(product);
        product.setName("更新商品");
        int updated = productMapper.update(product);
        Product found = productMapper.findById(product.getId());
        int deleted = productMapper.deleteById(product.getId());

        assertThat(inserted).isEqualTo(1);
        assertThat(updated).isEqualTo(1);
        assertThat(found.getName()).isEqualTo("更新商品");
        assertThat(deleted).isEqualTo(1);
        assertThat(productMapper.findById(product.getId())).isNull();
    }

    private Product product() {
        Product product = new Product();
        product.setName("测试商品");
        product.setCategory("测试分类");
        product.setPrice(new BigDecimal("9.90"));
        product.setStock(5);
        product.setImageUrl("image");
        product.setDescription("desc");
        return product;
    }
}
