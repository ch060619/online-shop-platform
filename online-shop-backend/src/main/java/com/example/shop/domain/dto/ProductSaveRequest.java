package com.example.shop.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 商品新增和更新请求。
 */
@Data
public class ProductSaveRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过 100 个字符")
    private String name;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 50, message = "商品分类不能超过 50 个字符")
    private String category;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于 0")
    private BigDecimal price;

    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于 0")
    private Integer stock;

    @Size(max = 255, message = "商品图片地址不能超过 255 个字符")
    private String imageUrl;

    @Size(max = 1000, message = "商品描述不能超过 1000 个字符")
    private String description;
}
