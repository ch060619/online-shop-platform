package com.example.shop.domain.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "页码不能小于 1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 50, message = "每页数量不能超过 50")
    private Integer pageSize = 6;
}
