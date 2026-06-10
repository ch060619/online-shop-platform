package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品 REST 控制器。
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "商品管理", description = "商品展示、详情和搜索接口")
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品控制器。
     *
     * @param productService 商品服务
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 查询商品列表。
     *
     * @param request 商品搜索请求
     * @return 商品分页列表响应
     */
    @GetMapping
    @Operation(summary = "分页查询商品列表", description = "支持按商品名称、分类、价格区间和页码搜索商品")
    public ApiResponse<PageVO<ProductVO>> listProducts(@Valid ProductSearchRequest request) {
        return ApiResponse.success(productService.search(request));
    }

    /**
     * 查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情响应
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询商品详情", description = "根据商品 ID 查询商品详细信息")
    public ApiResponse<ProductVO> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }
}
