package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.OrderItem;
import com.example.shop.domain.entity.OrderItemDetail;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单明细 MyBatis Mapper。
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 新增订单明细。
     *
     * @param orderItem 订单明细
     * @return 影响行数
     */
    @Insert("INSERT INTO order_item (order_id, product_id, product_name, product_image_url, price, quantity, subtotal) "
            + "VALUES (#{orderId}, #{productId}, #{productName}, #{productImageUrl}, #{price}, #{quantity}, "
            + "#{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem orderItem);

    /**
     * 根据订单 ID 查询订单明细。
     *
     * @param orderId 订单 ID
     * @return 订单明细列表
     */
    @Select("SELECT id, order_id, product_id, product_name, product_image_url, price, quantity, subtotal "
            + "FROM order_item WHERE order_id = #{orderId} ORDER BY id ASC")
    List<OrderItemDetail> findByOrderId(@Param("orderId") Long orderId);
}
