package com.example.shop.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 收货地址保存请求。
 */
@Data
public class AddressSaveRequest {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 30, message = "收货人不能超过 30 个字符")
    private String receiverName;

    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 200, message = "收货地址不能超过 200 个字符")
    private String receiverAddress;

    private Boolean defaultAddress;
}
