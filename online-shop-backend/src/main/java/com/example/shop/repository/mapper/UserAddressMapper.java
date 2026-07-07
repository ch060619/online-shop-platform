package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.UserAddress;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户收货地址 MyBatis Mapper。
 */
@Mapper
public interface UserAddressMapper {

    /**
     * 查询用户地址列表。
     *
     * @param userId 用户 ID
     * @return 地址列表
     */
    @Select("SELECT id, user_id, receiver_name, receiver_phone, receiver_address, "
            + "default_address, created_at, updated_at "
            + "FROM user_address WHERE user_id = #{userId} ORDER BY default_address DESC, id DESC")
    List<UserAddress> findByUserId(@Param("userId") Long userId);

    /**
     * 查询用户默认地址。
     *
     * @param userId 用户 ID
     * @return 默认地址，不存在时返回 null
     */
    @Select("SELECT id, user_id, receiver_name, receiver_phone, receiver_address, "
            + "default_address, created_at, updated_at "
            + "FROM user_address WHERE user_id = #{userId} AND default_address = 1 LIMIT 1")
    UserAddress findDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据 ID 和用户查询地址。
     *
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 地址实体，不存在时返回 null
     */
    @Select("SELECT id, user_id, receiver_name, receiver_phone, receiver_address, "
            + "default_address, created_at, updated_at "
            + "FROM user_address WHERE id = #{id} AND user_id = #{userId}")
    UserAddress findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 新增地址。
     *
     * @param address 地址实体
     * @return 影响行数
     */
    @Insert("INSERT INTO user_address "
            + "(user_id, receiver_name, receiver_phone, receiver_address, default_address) "
            + "VALUES (#{userId}, #{receiverName}, #{receiverPhone}, #{receiverAddress}, #{defaultAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAddress address);

    /**
     * 更新地址。
     *
     * @param address 地址实体
     * @return 影响行数
     */
    @Update("UPDATE user_address SET receiver_name = #{receiverName}, receiver_phone = #{receiverPhone}, "
            + "receiver_address = #{receiverAddress}, default_address = #{defaultAddress}, "
            + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
    int update(UserAddress address);

    /**
     * 清除用户默认地址标记。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Update("UPDATE user_address SET default_address = 0, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int clearDefault(@Param("userId") Long userId);

    /**
     * 删除地址。
     *
     * @param id 地址 ID
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_address WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
