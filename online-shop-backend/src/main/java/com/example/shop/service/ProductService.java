package com.example.shop.service;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.ProductVO;
import java.util.List;

/**
 * 商品服务接口。
 */
public interface ProductService {

    /**
     * 按条件查询商品。
     *
     * @param request 商品搜索请求
     * @return 商品列表
     */
    List<ProductVO> search(ProductSearchRequest request);

    /**
     * 查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    ProductVO getById(Long id);
}
