package com.example.shop.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shop.domain.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单主表 MyBatis Mapper。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

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

    /**
     * 修改订单收货信息。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @param receiverName 收货人
     * @param receiverPhone 联系方式
     * @param receiverAddress 收货地址
     * @return 影响行数
     */
    @Update("UPDATE orders SET receiver_name = #{receiverName}, receiver_phone = #{receiverPhone}, "
            + "receiver_address = #{receiverAddress} WHERE id = #{id} AND user_id = #{userId}")
    int updateReceiver(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("receiverName") String receiverName,
                       @Param("receiverPhone") String receiverPhone,
                       @Param("receiverAddress") String receiverAddress);

    /**
     * 删除指定用户的订单。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM orders WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
