package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.Order;
import com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单主表 MyBatis Mapper。
 */
@Mapper
public interface OrderMapper {

    /**
     * 查询用户订单列表。
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at, expire_at, paid_at, updated_at "
            + "FROM orders WHERE user_id = #{userId} ORDER BY id DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "status", property = "status"),
            @Result(column = "receiver_name", property = "receiverName"),
            @Result(column = "receiver_phone", property = "receiverPhone"),
            @Result(column = "receiver_address", property = "receiverAddress"),
            @Result(column = "created_at", property = "createdAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "expire_at", property = "expireAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "paid_at", property = "paidAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "updated_at", property = "updatedAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class)
    })
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 根据订单 ID 和用户 ID 查询订单。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @return 订单实体，不存在时返回 null
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at, expire_at, paid_at, updated_at "
            + "FROM orders WHERE id = #{id} AND user_id = #{userId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "status", property = "status"),
            @Result(column = "receiver_name", property = "receiverName"),
            @Result(column = "receiver_phone", property = "receiverPhone"),
            @Result(column = "receiver_address", property = "receiverAddress"),
            @Result(column = "created_at", property = "createdAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "expire_at", property = "expireAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "paid_at", property = "paidAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "updated_at", property = "updatedAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class)
    })
    Order findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据订单 ID 查询订单。
     *
     * @param id 订单 ID
     * @return 订单实体，不存在时返回 null
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at, expire_at, paid_at, updated_at FROM orders WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "status", property = "status"),
            @Result(column = "receiver_name", property = "receiverName"),
            @Result(column = "receiver_phone", property = "receiverPhone"),
            @Result(column = "receiver_address", property = "receiverAddress"),
            @Result(column = "created_at", property = "createdAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "expire_at", property = "expireAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "paid_at", property = "paidAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "updated_at", property = "updatedAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class)
    })
    Order findById(@Param("id") Long id);

    /**
     * 查询已超时但仍处于创建状态的订单。
     *
     * @param now 当前时间
     * @param status 当前状态
     * @param limit 查询数量上限
     * @return 超时订单列表
     */
    @Select("SELECT id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at, expire_at, paid_at, updated_at "
            + "FROM orders WHERE status = #{status} AND expire_at <= "
            + "#{now,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler} "
            + "ORDER BY expire_at ASC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "total_amount", property = "totalAmount"),
            @Result(column = "status", property = "status"),
            @Result(column = "receiver_name", property = "receiverName"),
            @Result(column = "receiver_phone", property = "receiverPhone"),
            @Result(column = "receiver_address", property = "receiverAddress"),
            @Result(column = "created_at", property = "createdAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "expire_at", property = "expireAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "paid_at", property = "paidAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class),
            @Result(column = "updated_at", property = "updatedAt",
                    typeHandler = SqliteLocalDateTimeTypeHandler.class)
    })
    List<Order> findExpiredCreatedOrders(@Param("now") LocalDateTime now,
                                         @Param("status") String status,
                                         @Param("limit") int limit);

    /**
     * 新增订单。
     *
     * @param order 订单实体
     * @return 影响行数
     */
    @Insert("INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, "
            + "receiver_address, created_at, expire_at, updated_at) "
            + "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, "
            + "#{receiverName}, #{receiverPhone}, #{receiverAddress}, "
            + "#{createdAt,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler}, "
            + "#{expireAt,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler}, "
            + "#{updatedAt,typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

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
     * 条件修改订单状态。
     *
     * @param id 订单 ID
     * @param userId 用户 ID
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param paidAt 支付时间
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    @Update("UPDATE orders SET status = #{toStatus}, paid_at = #{paidAt,"
            + "typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler}, "
            + "updated_at = #{updatedAt,"
            + "typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler} "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = #{fromStatus}")
    int updateStatusWhen(@Param("id") Long id,
                         @Param("userId") Long userId,
                         @Param("fromStatus") String fromStatus,
                         @Param("toStatus") String toStatus,
                         @Param("paidAt") LocalDateTime paidAt,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 条件修改订单状态，不限制用户。
     *
     * @param id 订单 ID
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    @Update("UPDATE orders SET status = #{toStatus}, updated_at = #{updatedAt,"
            + "typeHandler=com.example.shop.repository.typehandler.SqliteLocalDateTimeTypeHandler} "
            + "WHERE id = #{id} AND status = #{fromStatus}")
    int updateStatusByIdWhen(@Param("id") Long id,
                             @Param("fromStatus") String fromStatus,
                             @Param("toStatus") String toStatus,
                             @Param("updatedAt") LocalDateTime updatedAt);

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
