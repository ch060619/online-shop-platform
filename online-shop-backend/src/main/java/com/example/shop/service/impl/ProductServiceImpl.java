package com.example.shop.service.impl;

import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.dto.ProductSaveRequest;
import com.example.shop.domain.entity.Product;
import com.example.shop.domain.vo.PageVO;
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
     * @return 商品分页列表
     */
    @Override
    public PageVO<ProductVO> search(ProductSearchRequest request) {
        ProductSearchRequest actualRequest = request == null ? new ProductSearchRequest() : request;
        if (actualRequest.getMinPrice() != null && actualRequest.getMaxPrice() != null
                && actualRequest.getMinPrice().compareTo(actualRequest.getMaxPrice()) > 0) {
            throw new BusinessException("最低价格不能大于最高价格");
        }
        int page = actualRequest.getPage() == null ? 1 : actualRequest.getPage();
        int pageSize = actualRequest.getPageSize() == null ? 6 : actualRequest.getPageSize();
        int offset = (page - 1) * pageSize;
        long total = productMapper.count(
                actualRequest.getName(),
                actualRequest.getCategory(),
                actualRequest.getMinPrice(),
                actualRequest.getMaxPrice());
        List<ProductVO> items = productMapper.search(
                actualRequest.getName(),
                actualRequest.getCategory(),
                actualRequest.getMinPrice(),
                actualRequest.getMaxPrice(),
                pageSize,
                offset).stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());
        return PageVO.of(items, total, page, pageSize);
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

    /**
     * 新增商品。
     *
     * @param request 商品保存请求
     * @return 新增后的商品详情
     */
    @Override
    public ProductVO add(ProductSaveRequest request) {
        Product product = toProduct(request);
        productMapper.insert(product);
        return toProductVO(productMapper.findById(product.getId()));
    }

    /**
     * 更新商品。
     *
     * @param id 商品 ID
     * @param request 商品保存请求
     * @return 更新后的商品详情
     */
    @Override
    public ProductVO update(Long id, ProductSaveRequest request) {
        ensureProductExists(id);
        Product product = toProduct(request);
        product.setId(id);
        productMapper.update(product);
        return toProductVO(productMapper.findById(id));
    }

    /**
     * 删除商品。
     *
     * @param id 商品 ID
     */
    @Override
    public void delete(Long id) {
        ensureProductExists(id);
        productMapper.deleteById(id);
    }

    private void ensureProductExists(Long id) {
        if (productMapper.findById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
    }

    private Product toProduct(ProductSaveRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
        return product;
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
