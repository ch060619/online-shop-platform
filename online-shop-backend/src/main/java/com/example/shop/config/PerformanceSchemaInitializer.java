package com.example.shop.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 查询性能相关索引初始化器。
 */
@Component
public class PerformanceSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * 创建性能结构初始化器。
     *
     * @param jdbcTemplate JDBC 模板
     * @param dataSource 数据源
     */
    public PerformanceSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * 应用启动后补齐商品、购物车和订单链路查询索引。
     *
     * @param args 应用启动参数
     * @throws SQLException 当读取数据库元数据失败时抛出
     */
    @Override
    public void run(ApplicationArguments args) throws SQLException {
        if (isSqlite()) {
            createSqliteIndexes();
        }
        else {
            createMysqlIndexes();
        }
    }

    private void createSqliteIndexes() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_product_category_id ON product (category, id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_product_category_price_id "
                + "ON product (category, price, id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cart_item_user_id_id ON cart_item (user_id, id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_orders_user_id_id ON orders (user_id, id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_order_item_order_id_id ON order_item (order_id, id)");
    }

    private void createMysqlIndexes() {
        createMysqlIndexIfMissing(
                "product",
                "idx_product_category_id",
                "CREATE INDEX idx_product_category_id ON product (category, id)");
        createMysqlIndexIfMissing(
                "product",
                "idx_product_category_price_id",
                "CREATE INDEX idx_product_category_price_id ON product (category, price, id)");
        createMysqlIndexIfMissing(
                "cart_item",
                "idx_cart_item_user_id_id",
                "CREATE INDEX idx_cart_item_user_id_id ON cart_item (user_id, id)");
        createMysqlIndexIfMissing(
                "orders",
                "idx_orders_user_id_id",
                "CREATE INDEX idx_orders_user_id_id ON orders (user_id, id)");
        createMysqlIndexIfMissing(
                "order_item",
                "idx_order_item_order_id_id",
                "CREATE INDEX idx_order_item_order_id_id ON order_item (order_id, id)");
    }

    private void createMysqlIndexIfMissing(String tableName, String indexName, String createSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(createSql);
        }
    }

    private boolean isSqlite() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");
        }
    }
}
