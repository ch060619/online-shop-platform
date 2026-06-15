package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.PageMeta;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.dto.ProductSaveRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductCacheMetricsVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        PageVO<ProductVO> page = productService.search(request);
        return ApiResponse.successWithPage(page, PageMeta.from(page));
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

    /**
     * 查询商品缓存指标。
     *
     * @return 商品缓存指标响应
     */
    @GetMapping("/cache/metrics")
    @Operation(summary = "查询商品缓存指标", description = "返回商品列表和详情缓存的命中次数、未命中次数和命中率")
    public ApiResponse<ProductCacheMetricsVO> getCacheMetrics() {
        return ApiResponse.success(ProductCacheMetricsVO.from(productService.cacheMetrics()));
    }

    /**
     * 新增商品。
     *
     * @param request 商品保存请求
     * @return 新增商品响应
     */
    @PostMapping("/add")
    @Operation(summary = "新增商品", description = "新增一个商品基础信息")
    public ApiResponse<ProductVO> addProduct(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success("新增商品成功", productService.add(request));
    }

    /**
     * 删除商品。
     *
     * @param id 商品 ID
     * @return 删除结果响应
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除商品", description = "根据商品 ID 删除商品")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("删除商品成功", (Void) null);
    }

    /**
     * 更新商品。
     *
     * @param id 商品 ID
     * @param request 商品保存请求
     * @return 更新商品响应
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "更新商品", description = "根据商品 ID 更新商品基础信息")
    public ApiResponse<ProductVO> updateProduct(@PathVariable Long id,
                                                @Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.success("更新商品成功", productService.update(id, request));
    }

    /**
     * 兼容目标路径的商品分页查询。
     *
     * @param request 商品搜索请求
     * @return 商品分页列表响应
     */
    @GetMapping("/query")
    @Operation(summary = "查询商品", description = "兼容 /products/query 路径的商品分页查询")
    public ApiResponse<PageVO<ProductVO>> queryProducts(@Valid ProductSearchRequest request) {
        PageVO<ProductVO> page = productService.search(request);
        return ApiResponse.successWithPage(page, PageMeta.from(page));
    }
}
