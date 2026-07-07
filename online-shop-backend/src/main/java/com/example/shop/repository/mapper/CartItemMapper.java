package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.CartItem;
import com.example.shop.domain.entity.CartItemDetail;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 购物车 MyBatis Mapper。
 */
@Mapper
public interface CartItemMapper {

    /**
     * 查询指定用户的购物车明细详情。
     *
     * @param userId 用户 ID
     * @return 购物车明细详情列表
     */
    @Select("SELECT c.id, c.user_id, c.product_id, c.quantity, c.selected, p.name AS product_name, p.category, "
            + "p.price, p.stock, p.image_url, p.description "
            + "FROM cart_item c JOIN product p ON c.product_id = p.id "
            + "WHERE c.user_id = #{userId} ORDER BY c.created_at DESC, c.id DESC")
    List<CartItemDetail> findDetailsByUserId(@Param("userId") Long userId);

    /**
     * 查询指定用户选中的购物车明细详情。
     *
     * @param userId 用户 ID
     * @return 已选购物车明细详情列表
     */
    @Select("SELECT c.id, c.user_id, c.product_id, c.quantity, c.selected, p.name AS product_name, p.category, "
            + "p.price, p.stock, p.image_url, p.description "
            + "FROM cart_item c JOIN product p ON c.product_id = p.id "
            + "WHERE c.user_id = #{userId} AND c.selected = 1 ORDER BY c.created_at DESC, c.id DESC")
    List<CartItemDetail> findSelectedDetailsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户和商品查询购物车明细。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 购物车明细，不存在时返回 null
     */
    @Select("SELECT id, user_id, product_id, quantity, selected, created_at, updated_at "
            + "FROM cart_item WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据 ID 查询购物车明细。
     *
     * @param id 明细 ID
     * @param userId 用户 ID
     * @return 购物车明细，不存在时返回 null
     */
    @Select("SELECT id, user_id, product_id, quantity, selected, created_at, updated_at "
            + "FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    CartItem findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 新增购物车明细。
     *
     * @param cartItem 购物车明细
     * @return 影响行数
     */
    @Insert("INSERT INTO cart_item (user_id, product_id, quantity, selected) "
            + "VALUES (#{userId}, #{productId}, #{quantity}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    /**
     * 修改购物车明细数量。
     *
     * @param id 明细 ID
     * @param userId 用户 ID
     * @param quantity 数量
     * @return 影响行数
     */
    @Update("UPDATE cart_item SET quantity = #{quantity}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") Integer quantity);

    /**
     * 修改购物车明细选中状态。
     *
     * @param id 明细 ID
     * @param userId 用户 ID
     * @param selected 是否选中
     * @return 影响行数
     */
    @Update("UPDATE cart_item SET selected = #{selected}, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id} AND user_id = #{userId}")
    int updateSelected(@Param("id") Long id, @Param("userId") Long userId, @Param("selected") boolean selected);

    /**
     * 删除用户已选购物车明细。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId} AND selected = 1")
    int deleteSelectedByUserId(@Param("userId") Long userId);

    /**
     * 删除购物车明细。
     *
     * @param id 明细 ID
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清空用户购物车。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
