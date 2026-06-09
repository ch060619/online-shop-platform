package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单主表 MyBatis Mapper。
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单。
     *
     * @param order 订单实体
     * @return 影响行数
     */
    @Insert("INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address) VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, #{receiverName}, "
            + "#{receiverPhone}, #{receiverAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 查询用户订单列表。
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at FROM orders WHERE user_id = #{userId} ORDER BY id DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 根据订单 ID 和用户 ID 查询订单。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @return 订单实体，不存在时返回 null
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at FROM orders WHERE id = #{id} AND user_id = #{userId}")
    Order findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 修改订单状态。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @param status 新状态
     * @return 影响行数
     */
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status);
}
