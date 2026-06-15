package com.example.shop.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 鉴权相关表结构增量初始化器。
 */
@Component
public class AuthSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建鉴权结构初始化器。
     *
     * @param jdbcTemplate JDBC 模板
     * @param dataSource 数据源
     * @param passwordEncoder 密码编码器
     */
    public AuthSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource,
                                 PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 应用启动后补齐鉴权安全所需列、表和索引。
     *
     * @param args 应用启动参数
     * @throws SQLException 当读取数据库元数据失败时抛出
     */
    @Override
    public void run(ApplicationArguments args) throws SQLException {
        boolean sqlite = isSqlite();
        if (!columnExists("user", "role")) {
            jdbcTemplate.execute("ALTER TABLE " + tableName("user", sqlite) + " ADD COLUMN role "
                    + textType(sqlite, 20) + " NOT NULL DEFAULT 'USER'");
        }
        createRefreshTokenTable(sqlite);
        if (sqlite) {
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_token_hash "
                    + "ON refresh_token (token_hash)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_refresh_token_user "
                    + "ON refresh_token (user_id)");
        }
        else {
            createMysqlIndexIfMissing("uk_refresh_token_hash",
                    "CREATE UNIQUE INDEX uk_refresh_token_hash ON refresh_token (token_hash)");
            createMysqlIndexIfMissing("idx_refresh_token_user",
                    "CREATE INDEX idx_refresh_token_user ON refresh_token (user_id)");
        }
        ensureSeedUser(sqlite, "demo", "demo123", "演示用户", "13800000000", "USER");
        ensureSeedUser(sqlite, "admin", "admin123", "管理员", "13900000000", "ADMIN");
    }

    private void ensureSeedUser(boolean sqlite, String username, String rawPassword, String nickname,
                                String phone, String role) {
        String userTable = tableName("user", sqlite);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + userTable + " WHERE username = ?",
                Integer.class, username);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO " + userTable
                            + " (username, password, nickname, phone, role) VALUES (?, ?, ?, ?, ?)",
                    username, passwordEncoder.encode(rawPassword), nickname, phone, role);
            return;
        }
        String currentPassword = jdbcTemplate.queryForObject("SELECT password FROM " + userTable
                + " WHERE username = ?", String.class, username);
        String nextPassword = shouldMigratePassword(currentPassword, rawPassword)
                ? passwordEncoder.encode(rawPassword) : currentPassword;
        jdbcTemplate.update("UPDATE " + userTable
                        + " SET password = ?, nickname = ?, phone = ?, role = ? "
                        + "WHERE username = ?",
                nextPassword, nickname, phone, role, username);
    }

    private boolean shouldMigratePassword(String currentPassword, String rawPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            return true;
        }
        if (currentPassword.equals(rawPassword)) {
            return true;
        }
        return !currentPassword.startsWith("$2") && passwordEncoder.matches(rawPassword, currentPassword);
    }

    private void createRefreshTokenTable(boolean sqlite) {
        String idColumn = sqlite ? "id INTEGER PRIMARY KEY AUTOINCREMENT" : "`id` BIGINT NOT NULL AUTO_INCREMENT";
        String primaryKey = sqlite ? "" : ", PRIMARY KEY (`id`)";
        String engine = sqlite ? "" : " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + tableName("refresh_token", sqlite) + " ("
                + idColumn + ", "
                + columnName("token_hash", sqlite) + " " + textType(sqlite, 128) + " NOT NULL, "
                + columnName("user_id", sqlite) + " " + integerType(sqlite) + " NOT NULL, "
                + columnName("expires_at", sqlite) + " " + datetimeType(sqlite) + " NOT NULL, "
                + columnName("revoked", sqlite) + " " + booleanType(sqlite) + " NOT NULL DEFAULT 0, "
                + columnName("created_at", sqlite) + " " + datetimeType(sqlite) + " NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + primaryKey + ")" + engine);
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                var columns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }

    private boolean isSqlite() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");
        }
    }

    private void createMysqlIndexIfMissing(String indexName, String createSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = 'refresh_token' "
                        + "AND index_name = ?",
                Integer.class,
                indexName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(createSql);
        }
    }

    private String tableName(String name, boolean sqlite) {
        return sqlite ? name : "`" + name + "`";
    }

    private String columnName(String name, boolean sqlite) {
        return sqlite ? name : "`" + name + "`";
    }

    private String integerType(boolean sqlite) {
        return sqlite ? "INTEGER" : "BIGINT";
    }

    private String textType(boolean sqlite, int length) {
        return sqlite ? "TEXT" : "VARCHAR(" + length + ")";
    }

    private String datetimeType(boolean sqlite) {
        return sqlite ? "TEXT" : "DATETIME";
    }

    private String booleanType(boolean sqlite) {
        return sqlite ? "INTEGER" : "TINYINT(1)";
    }
}
