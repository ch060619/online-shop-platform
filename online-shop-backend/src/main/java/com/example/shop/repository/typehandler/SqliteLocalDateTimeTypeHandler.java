package com.example.shop.repository.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * SQLite 兼容的 LocalDateTime 类型处理器。
 */
@MappedTypes(LocalDateTime.class)
public class SqliteLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter WRITE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter READ_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    /**
     * 将 LocalDateTime 写入为 SQLite JDBC 可解析的文本格式。
     *
     * @param ps PreparedStatement
     * @param i 参数位置
     * @param parameter 时间值
     * @param jdbcType JDBC 类型
     * @throws SQLException SQL 写入异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps,
                                    int i,
                                    LocalDateTime parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(WRITE_FORMATTER));
    }

    /**
     * 从结果集中读取 LocalDateTime。
     *
     * @param rs 结果集
     * @param columnName 列名
     * @return 时间值，数据库值为空时返回 null
     * @throws SQLException SQL 读取异常
     */
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    /**
     * 从结果集中读取 LocalDateTime。
     *
     * @param rs 结果集
     * @param columnIndex 列序号
     * @return 时间值，数据库值为空时返回 null
     * @throws SQLException SQL 读取异常
     */
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    /**
     * 从存储过程结果中读取 LocalDateTime。
     *
     * @param cs CallableStatement
     * @param columnIndex 列序号
     * @return 时间值，数据库值为空时返回 null
     * @throws SQLException SQL 读取异常
     */
    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.trim().replace('T', ' '), READ_FORMATTER);
    }
}
