package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.config.ProductCacheProperties;
import com.example.shop.domain.dto.ProductSearchRequest;
import com.example.shop.domain.vo.PageVO;
import com.example.shop.domain.vo.ProductVO;
import com.example.shop.service.cache.ProductCacheMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * RedisProductCacheService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RedisProductCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private RedisProductCacheService cacheService;

    @BeforeEach
    void setUp() {
        ProductCacheProperties properties = new ProductCacheProperties();
        properties.setDetailTtlSeconds(10);
        properties.setDetailTtlRandomSeconds(2);
        properties.setListTtlSeconds(20);
        properties.setListTtlRandomSeconds(3);
        properties.setNullTtlSeconds(5);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        cacheService = new RedisProductCacheService(redisTemplate, new ObjectMapper(), properties);
    }

    @Test
    void should_returnProduct_when_detailCacheHit() throws Exception {
        String cachedProduct = new ObjectMapper().writeValueAsString(product());
        when(valueOperations.get("shop:product:detail:1")).thenReturn(cachedProduct);

        var lookup = cacheService.getProduct(1L);

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup.nullValue()).isFalse();
        assertThat(lookup.value()).hasValueSatisfying(product -> assertThat(product.getName()).isEqualTo("机械键盘"));
        assertThat(cacheService.metrics().detailHitRate()).isEqualTo(1.0D);
    }

    @Test
    void should_returnNullHit_when_nullCacheHit() {
        when(valueOperations.get("shop:product:detail:99")).thenReturn(null);
        when(valueOperations.get("shop:product:detail:null:99")).thenReturn("1");

        var lookup = cacheService.getProduct(99L);

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup.nullValue()).isTrue();
        assertThat(lookup.value()).isEmpty();
    }

    @Test
    void should_countMiss_when_detailCacheMissing() {
        when(valueOperations.get(anyString())).thenReturn(null);

        var lookup = cacheService.getProduct(1L);

        assertThat(lookup.hit()).isFalse();
        assertThat(cacheService.metrics()).isEqualTo(new ProductCacheMetrics(0, 1, 0, 0));
    }

    @Test
    void should_writeProductWithRandomizedTtl_when_putProduct() {
        cacheService.putProduct(1L, product());

        verify(valueOperations).set(
                eq("shop:product:detail:1"),
                anyString(),
                argThat(duration -> duration.compareTo(Duration.ofSeconds(10)) >= 0
                        && duration.compareTo(Duration.ofSeconds(12)) <= 0));
        verify(redisTemplate).delete("shop:product:detail:null:1");
    }

    @Test
    void should_writeNullCacheWithShortTtl_when_putNullProduct() {
        cacheService.putNullProduct(99L);

        verify(valueOperations).set("shop:product:detail:null:99", "1", Duration.ofSeconds(5));
    }

    @Test
    void should_returnProductList_when_listCacheHit() throws Exception {
        PageVO<ProductVO> page = PageVO.of(List.of(product()), 1, 1, 6);
        ProductSearchRequest request = searchRequest();
        cacheService.putProductList(request, page);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), anyString(), any(Duration.class));
        when(valueOperations.get(keyCaptor.getValue())).thenReturn(redisValueForStoredList(page));

        var cached = cacheService.getProductList(request);

        assertThat(cached).isPresent();
        assertThat(cached.get().getItems()).hasSize(1);
        assertThat(cached.get().getTotal()).isEqualTo(1);
    }

    @Test
    void should_evictTrackedListKeys_when_evictProductLists() {
        when(setOperations.members("shop:product:list:keys"))
                .thenReturn(new LinkedHashSet<>(List.of("list:1", "list:2")));

        cacheService.evictProductLists();

        verify(redisTemplate).delete(List.of("list:1", "list:2"));
        verify(redisTemplate).delete("shop:product:list:keys");
    }

    @Test
    void should_fallbackToMiss_when_redisFails() {
        when(valueOperations.get("shop:product:detail:1")).thenThrow(new IllegalStateException("redis down"));

        var lookup = cacheService.getProduct(1L);

        assertThat(lookup.hit()).isFalse();
    }

    @Test
    void should_ignoreWriteFailure_when_redisFails() {
        doThrow(new IllegalStateException("redis down"))
                .when(valueOperations)
                .set(eq("shop:product:detail:1"), anyString(), any(Duration.class));

        cacheService.putProduct(1L, product());

        assertThat(cacheService.metrics().detailHitRate()).isZero();
    }

    private String redisValueForStoredList(PageVO<ProductVO> page) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(new ProductPagePayload(
                page.getItems(), page.getTotal(), page.getPage(), page.getPageSize()));
    }

    private ProductSearchRequest searchRequest() {
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("键盘");
        request.setCategory("数码配件");
        return request;
    }

    private ProductVO product() {
        ProductVO vo = new ProductVO();
        vo.setId(1L);
        vo.setName("机械键盘");
        vo.setCategory("数码配件");
        vo.setPrice(new BigDecimal("299.00"));
        vo.setStock(10);
        vo.setImageUrl("image");
        vo.setDescription("desc");
        return vo;
    }

    private record ProductPagePayload(List<ProductVO> items, long total, int page, int pageSize) {
    }
}
