package com.example.shop.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单表增量结构初始化器。
 */
@Component
public class OrderSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * 创建订单结构初始化器。
     *
     * @param jdbcTemplate JDBC 模板
     * @param dataSource 数据源
     */
    public OrderSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * 应用启动后补齐订单状态机所需列和索引。
     *
     * @param args 应用启动参数
     * @throws SQLException 当读取数据库元数据失败时抛出
     */
    @Override
    public void run(ApplicationArguments args) throws SQLException {
        boolean sqlite = isSqlite();
        if (!columnExists("expire_at")) {
            jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN expire_at " + datetimeType(sqlite));
        }
        if (!columnExists("paid_at")) {
            jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN paid_at " + datetimeType(sqlite));
        }
        if (!columnExists("updated_at")) {
            jdbcTemplate.execute("ALTER TABLE orders ADD COLUMN updated_at " + datetimeType(sqlite));
            jdbcTemplate.execute("UPDATE orders SET updated_at = created_at WHERE updated_at IS NULL");
        }
        if (sqlite) {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_orders_status_expire_at "
                    + "ON orders (status, expire_at)");
        }
        else {
            createMysqlIndexIfMissing();
        }
    }

    private boolean columnExists(String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                var columns = connection.getMetaData().getColumns(null, null, "orders", columnName)) {
            return columns.next();
        }
    }

    private boolean isSqlite() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");
        }
    }

    private String datetimeType(boolean sqlite) {
        return sqlite ? "TEXT" : "DATETIME";
    }

    private void createMysqlIndexIfMissing() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = 'orders' "
                        + "AND index_name = 'idx_orders_status_expire_at'",
                Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute("CREATE INDEX idx_orders_status_expire_at ON orders (status, expire_at)");
        }
    }
}
