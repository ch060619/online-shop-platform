package com.example.shop.service.impl;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.entity.Product;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.ProductMapper;
import com.example.shop.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 商品服务实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    /**
     * 创建商品服务实现。
     *
     * @param productMapper 商品 Mapper
     */
    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 按条件查询商品。
     *
     * @param request 商品搜索请求
     * @return 商品列表
     */
    @Override
    public List<ProductVO> search(ProductSearchRequest request) {
        ProductSearchRequest actualRequest = request == null ? new ProductSearchRequest() : request;
        if (actualRequest.getMinPrice() != null && actualRequest.getMaxPrice() != null
                && actualRequest.getMinPrice().compareTo(actualRequest.getMaxPrice()) > 0) {
            throw new BusinessException("最低价格不能大于最高价格");
        }
        return productMapper.search(
                actualRequest.getName(),
                actualRequest.getCategory(),
                actualRequest.getMinPrice(),
                actualRequest.getMaxPrice()).stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return toProductVO(product);
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCategory(product.getCategory());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setImageUrl(product.getImageUrl());
        vo.setDescription(product.getDescription());
        return vo;
    }
}
