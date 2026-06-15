package com.example.shop.service;

import java.util.Optional;

/**
 * 商品缓存查找结果。
 *
 * @param hit 是否命中缓存
 * @param nullValue 是否命中空值缓存
 * @param value 缓存值
 * @param <T> 缓存值类型
 */
public record ProductCacheLookup<T>(boolean hit, boolean nullValue, Optional<T> value) {

    /**
     * 创建未命中结果。
     *
     * @param <T> 缓存值类型
     * @return 未命中结果
     */
    public static <T> ProductCacheLookup<T> miss() {
        return new ProductCacheLookup<>(false, false, Optional.empty());
    }

    /**
     * 创建空值命中结果。
     *
     * @param <T> 缓存值类型
     * @return 空值命中结果
     */
    public static <T> ProductCacheLookup<T> nullHit() {
        return new ProductCacheLookup<>(true, true, Optional.empty());
    }

    /**
     * 创建正常命中结果。
     *
     * @param value 缓存值
     * @param <T> 缓存值类型
     * @return 命中结果
     */
    public static <T> ProductCacheLookup<T> hit(T value) {
        return new ProductCacheLookup<>(true, false, Optional.of(value));
    }
}
