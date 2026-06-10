package com.example.shop.domain.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/**
 * 商品搜索请求。
 */
@Data
public class ProductSearchRequest {

    private String name;
    private String category;

    @DecimalMin(value = "0.00", message = "最低价格不能小于 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.00", message = "最高价格不能小于 0")
    private BigDecimal maxPrice;
}
