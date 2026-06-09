package com.example.shop.domain.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 购物车响应对象。
 */
@Data
public class CartVO {

    private List<CartItemVO> items = new ArrayList<>();
    private Integer totalQuantity = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
