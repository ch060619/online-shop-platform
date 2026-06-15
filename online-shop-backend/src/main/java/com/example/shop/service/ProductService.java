package com.example.shop.service;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.dto.ProductSaveRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.cache.ProductCacheMetrics;

/**
 * 商品服务接口。
 */
public interface ProductService {

    /**
     * 按条件查询商品。
     *
     * @param request 商品搜索请求
     * @return 商品分页列表
     */
    PageVO<ProductVO> search(ProductSearchRequest request);

    /**
     * 查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    ProductVO getById(Long id);

    /**
     * 新增商品。
     *
     * @param request 商品保存请求
     * @return 新增后的商品详情
     */
    ProductVO add(ProductSaveRequest request);

    /**
     * 更新商品。
     *
     * @param id 商品 ID
     * @param request 商品保存请求
     * @return 更新后的商品详情
     */
    ProductVO update(Long id, ProductSaveRequest request);

    /**
     * 删除商品。
     *
     * @param id 商品 ID
     */
    void delete(Long id);

    /**
     * 获取商品缓存指标快照。
     *
     * @return 商品缓存指标
     */
    ProductCacheMetrics cacheMetrics();
}
