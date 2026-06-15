package com.example.shop.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 商品缓存配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.cache.product")
public class ProductCacheProperties {

    private long detailTtlSeconds = 600;
    private long detailTtlRandomSeconds = 120;
    private long listTtlSeconds = 180;
    private long listTtlRandomSeconds = 60;
    private long nullTtlSeconds = 60;
    private List<Long> preloadProductIds = new ArrayList<>();
}
