package com.example.shop.domain.vo;

import lombok.Data;

/**
 * 收货地址响应对象。
 */
@Data
public class AddressVO {

    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Boolean defaultAddress;
}
