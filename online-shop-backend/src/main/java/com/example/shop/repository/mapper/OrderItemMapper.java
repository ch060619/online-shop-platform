package com.example.shop.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shop.domain.entity.OrderItem;
import com.example.shop.domain.entity.OrderItemDetail;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单明细 MyBatis Mapper。
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单 ID 查询订单明细。
     *
     * @param orderId 订单 ID
     * @return 订单明细列表
     */
    @Select("SELECT id, order_id, product_id, product_name, product_image_url, price, quantity, subtotal "
            + "FROM order_item WHERE order_id = #{orderId} ORDER BY id ASC")
    List<OrderItemDetail> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 删除指定订单的全部明细。
     *
     * @param orderId 订单 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);
}
