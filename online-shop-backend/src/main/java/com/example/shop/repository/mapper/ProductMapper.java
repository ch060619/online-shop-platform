package com.example.shop.repository.mapper;

import com.example.shop.domain.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 MyBatis Mapper。
 */
@Mapper
public interface ProductMapper {

    /**
     * 按条件查询商品。
     *
     * @param name 商品名关键词
     * @param category 分类
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    @Select("<script>"
            + "SELECT id, name, category, price, stock, image_url, description, created_at "
            + "FROM product WHERE 1=1 "
            + "<if test='name != null and name != \"\"'>AND name LIKE '%' || #{name} || '%' </if>"
            + "<if test='category != null and category != \"\"'>AND category = #{category} </if>"
            + "<if test='minPrice != null'>AND price &gt;= #{minPrice} </if>"
            + "<if test='maxPrice != null'>AND price &lt;= #{maxPrice} </if>"
            + "ORDER BY id ASC"
            + "</script>")
    List<Product> search(@Param("name") String name,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 根据 ID 查询商品。
     *
     * @param id 商品 ID
     * @return 商品实体，不存在时返回 null
     */
    @Select("SELECT id, name, category, price, stock, image_url, description, created_at FROM product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    /**
     * 扣减库存。
     *
     * @param id 商品 ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回补库存。
     *
     * @param id 商品 ID
     * @param quantity 回补数量
     * @return 影响行数
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 新增商品，主要用于测试。
     *
     * @param product 商品实体
     * @return 影响行数
     */
    @Insert("INSERT INTO product (name, category, price, stock, image_url, description) "
            + "VALUES (#{name}, #{category}, #{price}, #{stock}, #{imageUrl}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);
}
