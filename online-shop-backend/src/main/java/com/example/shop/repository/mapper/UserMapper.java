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
    @Select("SELECT id, username, password, nickname, phone, role, created_at FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    @Select("SELECT id, username, password, nickname, phone, role, created_at FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);
}
