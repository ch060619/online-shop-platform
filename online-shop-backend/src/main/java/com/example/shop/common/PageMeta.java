package com.example.shop.common;

import com.example.shop.domain.vo.PageVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应中的分页元信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {

    private int page;
    private int pageSize;
    private long total;
    private int totalPages;

    /**
     * 从分页响应对象创建分页元信息。
     *
     * @param pageVO 分页响应对象
     * @return 分页元信息
     */
    public static PageMeta from(PageVO<?> pageVO) {
        return new PageMeta(pageVO.getPage(), pageVO.getPageSize(), pageVO.getTotal(), pageVO.getTotalPages());
    }
}
