package com.example.shop.service.cache;

/**
 * 商品缓存指标快照。
 *
 * @param detailHits 商品详情命中次数
 * @param detailMisses 商品详情未命中次数
 * @param listHits 商品列表命中次数
 * @param listMisses 商品列表未命中次数
 */
public record ProductCacheMetrics(long detailHits, long detailMisses, long listHits, long listMisses) {

    /**
     * 计算商品详情缓存命中率。
     *
     * @return 详情缓存命中率
     */
    public double detailHitRate() {
        long total = detailHits + detailMisses;
        return total == 0 ? 0.0D : (double) detailHits / total;
    }

    /**
     * 计算商品列表缓存命中率。
     *
     * @return 列表缓存命中率
     */
    public double listHitRate() {
        long total = listHits + listMisses;
        return total == 0 ? 0.0D : (double) listHits / total;
    }
}
