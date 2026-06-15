package com.example.shop.service.impl;

import com.example.shop.config.ProductCacheProperties;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.dto.ProductSaveRequest;
import com.example.shop.domain.entity.Product;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.ProductMapper;
import com.example.shop.service.ProductCacheLookup;
import com.example.shop.service.ProductCacheService;
import com.example.shop.service.ProductService;
import com.example.shop.service.cache.ProductCacheMetrics;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 商品服务实现。
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductCacheProperties cacheProperties;

    /**
     * 创建商品服务实现。
     *
     * @param productMapper 商品 Mapper
     * @param productCacheService 商品缓存服务
     * @param cacheProperties 商品缓存配置
     */
    public ProductServiceImpl(ProductMapper productMapper,
                              ProductCacheService productCacheService,
                              ProductCacheProperties cacheProperties) {
        this.productMapper = productMapper;
        this.productCacheService = productCacheService;
        this.cacheProperties = cacheProperties;
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
        actualRequest.setPage(page);
        actualRequest.setPageSize(pageSize);
        var cached = productCacheService.getProductList(actualRequest);
        if (cached.isPresent()) {
            return cached.get();
        }
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
        PageVO<ProductVO> pageResult = PageVO.of(items, total, page, pageSize);
        productCacheService.putProductList(actualRequest, pageResult);
        return pageResult;
    }

    /**
     * 查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @Override
    public ProductVO getById(Long id) {
        ProductCacheLookup<ProductVO> cached = productCacheService.getProduct(id);
        if (cached.hit()) {
            if (cached.nullValue()) {
                throw new BusinessException(404, "商品不存在");
            }
            return cached.value().orElseThrow(() -> new BusinessException(404, "商品不存在"));
        }
        Product product = productMapper.findById(id);
        if (product == null) {
            productCacheService.putNullProduct(id);
            throw new BusinessException(404, "商品不存在");
        }
        ProductVO vo = toProductVO(product);
        productCacheService.putProduct(id, vo);
        return vo;
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
        ProductVO vo = toProductVO(productMapper.findById(product.getId()));
        invalidateProductCache(product.getId());
        return vo;
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
        ProductVO vo = toProductVO(productMapper.findById(id));
        invalidateProductCache(id);
        return vo;
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
        invalidateProductCache(id);
    }

    /**
     * 获取商品缓存指标快照。
     *
     * @return 商品缓存指标
     */
    @Override
    public ProductCacheMetrics cacheMetrics() {
        return productCacheService.metrics();
    }

    /**
     * 预热热点商品详情缓存。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadHotProducts() {
        for (Long productId : cacheProperties.getPreloadProductIds()) {
            try {
                getById(productId);
            }
            catch (BusinessException exception) {
                // Missing hot products are already protected by getById null cache.
            }
        }
    }

    private void ensureProductExists(Long id) {
        if (productMapper.findById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
    }

    private void invalidateProductCache(Long id) {
        productCacheService.evictProduct(id);
        productCacheService.evictProductLists();
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
