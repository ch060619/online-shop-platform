package com.example.shop.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * SQL 查询计划测试。
 */
@SpringBootTest
@ActiveProfiles("sqlite")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlExplainPlanTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_useProductCategoryIndex_when_queryProductList() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT id, name, category, price, stock, image_url, description, created_at "
                + "FROM product WHERE category = '数码配件' ORDER BY id ASC LIMIT 6 OFFSET 0");

        assertThat(plan).contains("idx_product_category_id");
    }

    @Test
    void should_useProductCategoryPriceIndex_when_queryProductListByPriceRange() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT id, name, category, price, stock, image_url, description, created_at "
                + "FROM product WHERE category = '数码配件' AND price >= 100 AND price <= 800 "
                + "ORDER BY id ASC LIMIT 6 OFFSET 0");

        assertThat(plan).contains("idx_product_category_price_id");
    }

    @Test
    void should_useCartUserIndex_when_queryCartDetails() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, p.category, "
                + "p.price, p.stock, p.image_url, p.description "
                + "FROM cart_item c JOIN product p ON c.product_id = p.id "
                + "WHERE c.user_id = 1 ORDER BY c.id ASC");

        assertThat(plan).contains("idx_cart_item_user_id_id");
    }

    @Test
    void should_useOrderUserIndex_when_queryUserOrders() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
                + "receiver_address, created_at, expire_at, paid_at, updated_at "
                + "FROM orders WHERE user_id = 1 ORDER BY id DESC");

        assertThat(plan).contains("idx_orders_user_id_id");
    }

    @Test
    void should_useOrderItemIndex_when_queryOrderItems() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT id, order_id, product_id, product_name, product_image_url, price, quantity, subtotal "
                + "FROM order_item WHERE order_id = 1 ORDER BY id ASC");

        assertThat(plan).contains("idx_order_item_order_id_id");
    }

    @Test
    void should_useTimeoutIndex_when_queryExpiredOrders() {
        String plan = explain("EXPLAIN QUERY PLAN "
                + "SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
                + "receiver_address, created_at, expire_at, paid_at, updated_at "
                + "FROM orders WHERE status = 'CREATED' AND expire_at <= '2026-06-15 00:00:00' "
                + "ORDER BY expire_at ASC LIMIT 100");

        assertThat(plan).contains("idx_orders_status_expire_at");
    }

    private String explain(String sql) {
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("detail"));
        return rows.stream().collect(Collectors.joining(" | "));
    }
}
