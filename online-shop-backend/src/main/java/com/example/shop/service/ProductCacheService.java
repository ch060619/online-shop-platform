package com.example.shop.service;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.cache.ProductCacheMetrics;
import java.util.Optional;

/**
 * 商品缓存服务接口。
 */
public interface ProductCacheService {

    /**
     * 查询商品详情缓存。
     *
     * @param id 商品 ID
     * @return 缓存查找结果
     */
    ProductCacheLookup<ProductVO> getProduct(Long id);

    /**
     * 写入商品详情缓存。
     *
     * @param id 商品 ID
     * @param product 商品详情
     */
    void putProduct(Long id, ProductVO product);

    /**
     * 写入空值缓存。
     *
     * @param id 商品 ID
     */
    void putNullProduct(Long id);

    /**
     * 查询商品列表缓存。
     *
     * @param request 商品搜索请求
     * @return 商品列表缓存
     */
    Optional<PageVO<ProductVO>> getProductList(ProductSearchRequest request);

    /**
     * 写入商品列表缓存。
     *
     * @param request 商品搜索请求
     * @param page 商品分页结果
     */
    void putProductList(ProductSearchRequest request, PageVO<ProductVO> page);

    /**
     * 删除商品详情缓存。
     *
     * @param id 商品 ID
     */
    void evictProduct(Long id);

    /**
     * 删除全部商品列表缓存。
     */
    void evictProductLists();

    /**
     * 获取缓存指标快照。
     *
     * @return 商品缓存指标
     */
    ProductCacheMetrics metrics();
}
