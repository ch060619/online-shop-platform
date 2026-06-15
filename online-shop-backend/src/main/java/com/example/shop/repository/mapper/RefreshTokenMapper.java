package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.RefreshToken;
import com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 刷新令牌 MyBatis Mapper。
 */
@Mapper
public interface RefreshTokenMapper {

    /**
     * 新增刷新令牌。
     *
     * @param refreshToken 刷新令牌实体
     * @return 影响行数
     */
    @Insert("INSERT INTO refresh_token (token_hash, user_id, expires_at, revoked, created_at) "
            + "VALUES (#{tokenHash}, #{userId}, "
            + "#{expiresAt,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler}, "
            + "#{revoked}, "
            + "#{createdAt,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RefreshToken refreshToken);

    /**
     * 查询未撤销且未过期的刷新令牌。
     *
     * @param tokenHash 刷新令牌摘要
     * @param now 当前时间
     * @return 刷新令牌实体，不存在时返回 null
     */
    @Select("SELECT id, token_hash, user_id, expires_at, revoked, created_at "
            + "FROM refresh_token WHERE token_hash = #{tokenHash} AND revoked = 0 AND expires_at > "
            + "#{now,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "token_hash", property = "tokenHash"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "expires_at", property = "expiresAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "revoked", property = "revoked"),
            @Result(column = "created_at", property = "createdAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class)
    })
    RefreshToken findActiveByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    /**
     * 撤销刷新令牌。
     *
     * @param tokenHash 刷新令牌摘要
     * @return 影响行数
     */
    @Update("UPDATE refresh_token SET revoked = 1 WHERE token_hash = #{tokenHash} AND revoked = 0")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash);
}
