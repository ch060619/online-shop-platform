package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT id, username, password, nickname, phone, role, points, created_at FROM user "
            + "WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    @Select("SELECT id, username, password, nickname, phone, role, points, created_at FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 新增用户。
     *
     * @param user 用户实体
     * @return 影响行数
     */
    @Insert("INSERT INTO user (username, password, nickname, phone, role, points) "
            + "VALUES (#{username}, #{password}, #{nickname}, #{phone}, #{role}, #{points})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 修改用户密码。
     *
     * @param id 用户 ID
     * @param password 新密码 hash
     * @return 影响行数
     */
    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
