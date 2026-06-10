package com.example.shop.domain.vo;

import java.util.List;
import lombok.Getter;

/**
 * 分页响应对象。
 *
 * @param <T> 分页条目类型
 */
@Getter
public final class PageVO<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    private PageVO(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建分页响应。
     *
     * @param items 当前页条目
     * @param total 总条目数
     * @param page 当前页码
     * @param pageSize 每页条目数
     * @param <T> 分页条目类型
     * @return 分页响应
     */
    public static <T> PageVO<T> of(List<T> items, long total, int page, int pageSize) {
        return new PageVO<>(items, total, page, pageSize);
    }
}
