package com.example.shop.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * PerformanceSchemaInitializer 测试。
 */
@SpringBootTest
@ActiveProfiles("sqlite")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PerformanceSchemaInitializerTest {

    private static final ApplicationArguments EMPTY_ARGS = new DefaultApplicationArguments();

    @Autowired
    private PerformanceSchemaInitializer initializer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_createPerformanceIndexes_when_sqliteDatabaseStarts() throws SQLException {
        initializer.run(EMPTY_ARGS);

        assertThat(indexExists("idx_product_category_id")).isTrue();
        assertThat(indexExists("idx_product_category_price_id")).isTrue();
        assertThat(indexExists("idx_cart_item_user_id_id")).isTrue();
        assertThat(indexExists("idx_orders_user_id_id")).isTrue();
        assertThat(indexExists("idx_order_item_order_id_id")).isTrue();
    }

    @Test
    void should_notFail_when_initializerRunsTwice() throws SQLException {
        initializer.run(EMPTY_ARGS);
        initializer.run(EMPTY_ARGS);

        assertThat(indexExists("idx_product_category_id")).isTrue();
    }

    private boolean indexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sqlite_master WHERE type = 'index' AND name = ?",
                Integer.class,
                indexName);
        return count != null && count > 0;
    }
}
