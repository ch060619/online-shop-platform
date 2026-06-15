package com.example.shop.domain.vo;

import com.example.shop.service.cache.ProductCacheMetrics;

/**
 * 商品缓存指标响应。
 *
 * @param detailHits 商品详情缓存命中次数
 * @param detailMisses 商品详情缓存未命中次数
 * @param detailHitRate 商品详情缓存命中率
 * @param listHits 商品列表缓存命中次数
 * @param listMisses 商品列表缓存未命中次数
 * @param listHitRate 商品列表缓存命中率
 */
public record ProductCacheMetricsVO(long detailHits,
                                    long detailMisses,
                                    double detailHitRate,
                                    long listHits,
                                    long listMisses,
                                    double listHitRate) {

    /**
     * 从缓存指标快照转换为响应对象。
     *
     * @param metrics 缓存指标快照
     * @return 商品缓存指标响应
     */
    public static ProductCacheMetricsVO from(ProductCacheMetrics metrics) {
        return new ProductCacheMetricsVO(
                metrics.detailHits(),
                metrics.detailMisses(),
                metrics.detailHitRate(),
                metrics.listHits(),
                metrics.listMisses(),
                metrics.listHitRate());
    }
}
