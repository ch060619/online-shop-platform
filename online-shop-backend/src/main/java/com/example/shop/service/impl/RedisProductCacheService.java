package com.example.shop.service.impl;

import com.example.shop.config.ProductCacheProperties;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.ProductCacheLookup;
import com.example.shop.service.ProductCacheService;
import com.example.shop.service.cache.ProductCacheMetrics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 商品缓存服务实现。
 */
@Service
public class RedisProductCacheService implements ProductCacheService {

    private static final String DETAIL_KEY_PREFIX = "shop:product:detail:";
    private static final String NULL_DETAIL_KEY_PREFIX = "shop:product:detail:null:";
    private static final String LIST_KEY_PREFIX = "shop:product:list:";
    private static final String LIST_KEYS = "shop:product:list:keys";
    private static final String NULL_VALUE = "1";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductCacheProperties properties;
    private final AtomicLong detailHits = new AtomicLong();
    private final AtomicLong detailMisses = new AtomicLong();
    private final AtomicLong listHits = new AtomicLong();
    private final AtomicLong listMisses = new AtomicLong();

    /**
     * 创建 Redis 商品缓存服务。
     *
     * @param redisTemplate Redis 字符串模板
     * @param objectMapper JSON 序列化器
     * @param properties 商品缓存配置
     */
    public RedisProductCacheService(StringRedisTemplate redisTemplate,
                                    ObjectMapper objectMapper,
                                    ProductCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 查询商品详情缓存。
     *
     * @param id 商品 ID
     * @return 缓存查找结果
     */
    @Override
    public ProductCacheLookup<ProductVO> getProduct(Long id) {
        try {
            String cached = redisTemplate.opsForValue().get(detailKey(id));
            if (cached != null) {
                detailHits.incrementAndGet();
                return ProductCacheLookup.hit(objectMapper.readValue(cached, ProductVO.class));
            }
            String nullCached = redisTemplate.opsForValue().get(nullDetailKey(id));
            if (nullCached != null) {
                detailHits.incrementAndGet();
                return ProductCacheLookup.nullHit();
            }
        }
        catch (RuntimeException | JsonProcessingException exception) {
            return ProductCacheLookup.miss();
        }
        detailMisses.incrementAndGet();
        return ProductCacheLookup.miss();
    }

    /**
     * 写入商品详情缓存。
     *
     * @param id 商品 ID
     * @param product 商品详情
     */
    @Override
    public void putProduct(Long id, ProductVO product) {
        try {
            redisTemplate.opsForValue().set(
                    detailKey(id),
                    objectMapper.writeValueAsString(product),
                    ttl(properties.getDetailTtlSeconds(), properties.getDetailTtlRandomSeconds()));
            redisTemplate.delete(nullDetailKey(id));
        }
        catch (RuntimeException | JsonProcessingException exception) {
            // Cache failure must not fail the product query.
        }
    }

    /**
     * 写入空值缓存。
     *
     * @param id 商品 ID
     */
    @Override
    public void putNullProduct(Long id) {
        try {
            redisTemplate.opsForValue().set(
                    nullDetailKey(id), NULL_VALUE, Duration.ofSeconds(properties.getNullTtlSeconds()));
        }
        catch (RuntimeException exception) {
            // Cache failure must not fail the product query.
        }
    }

    /**
     * 查询商品列表缓存。
     *
     * @param request 商品搜索请求
     * @return 商品列表缓存
     */
    @Override
    public Optional<PageVO<ProductVO>> getProductList(ProductSearchRequest request) {
        try {
            String cached = redisTemplate.opsForValue().get(listKey(request));
            if (cached != null) {
                listHits.incrementAndGet();
                ProductPagePayload payload = objectMapper.readValue(cached, ProductPagePayload.class);
                return Optional.of(PageVO.of(payload.items(), payload.total(), payload.page(), payload.pageSize()));
            }
        }
        catch (RuntimeException | JsonProcessingException exception) {
            return Optional.empty();
        }
        listMisses.incrementAndGet();
        return Optional.empty();
    }

    /**
     * 写入商品列表缓存。
     *
     * @param request 商品搜索请求
     * @param page 商品分页结果
     */
    @Override
    public void putProductList(ProductSearchRequest request, PageVO<ProductVO> page) {
        try {
            String key = listKey(request);
            ProductPagePayload payload = new ProductPagePayload(
                    page.getItems(), page.getTotal(), page.getPage(), page.getPageSize());
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(payload),
                    ttl(properties.getListTtlSeconds(), properties.getListTtlRandomSeconds()));
            redisTemplate.opsForSet().add(LIST_KEYS, key);
        }
        catch (RuntimeException | JsonProcessingException exception) {
            // Cache failure must not fail the product query.
        }
    }

    /**
     * 删除商品详情缓存。
     *
     * @param id 商品 ID
     */
    @Override
    public void evictProduct(Long id) {
        try {
            redisTemplate.delete(List.of(detailKey(id), nullDetailKey(id)));
        }
        catch (RuntimeException exception) {
            // Cache failure must not fail the product write operation.
        }
    }

    /**
     * 删除全部商品列表缓存。
     */
    @Override
    public void evictProductLists() {
        try {
            var members = redisTemplate.opsForSet().members(LIST_KEYS);
            List<String> keys = members == null ? List.of() : members.stream().toList();
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete(LIST_KEYS);
        }
        catch (RuntimeException exception) {
            // Cache failure must not fail the product write operation.
        }
    }

    /**
     * 获取缓存指标快照。
     *
     * @return 商品缓存指标
     */
    @Override
    public ProductCacheMetrics metrics() {
        return new ProductCacheMetrics(
                detailHits.get(), detailMisses.get(), listHits.get(), listMisses.get());
    }

    private Duration ttl(long baseSeconds, long randomSeconds) {
        long extraSeconds = randomSeconds <= 0 ? 0 : ThreadLocalRandom.current().nextLong(randomSeconds + 1);
        return Duration.ofSeconds(baseSeconds + extraSeconds);
    }

    private String detailKey(Long id) {
        return DETAIL_KEY_PREFIX + id;
    }

    private String nullDetailKey(Long id) {
        return NULL_DETAIL_KEY_PREFIX + id;
    }

    private String listKey(ProductSearchRequest request) {
        return LIST_KEY_PREFIX + sha256(searchSignature(request));
    }

    private String searchSignature(ProductSearchRequest request) {
        return String.join("|",
                String.valueOf(request.getName()),
                String.valueOf(request.getCategory()),
                String.valueOf(request.getMinPrice()),
                String.valueOf(request.getMaxPrice()),
                String.valueOf(request.getPage()),
                String.valueOf(request.getPageSize()));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }

    private record ProductPagePayload(List<ProductVO> items, long total, int page, int pageSize) {
    }
}
