package com.example.shop.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 商品响应对象。
 */
@Data
public class ProductVO {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String description;
}
