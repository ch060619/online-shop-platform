package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 MyBatis Mapper。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    @Select("SELECT id, username, password, nickname, phone, created_at FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
}
