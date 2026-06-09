package com.example.shop.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 创建订单请求。
 */
@Data
public class CreateOrderRequest {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人不能超过 50 个字符")
    private String receiverName;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 30, message = "联系方式不能超过 30 个字符")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址不能超过 200 个字符")
    private String receiverAddress;
}
