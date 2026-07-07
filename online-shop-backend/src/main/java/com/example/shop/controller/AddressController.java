package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.AddressSaveRequest;
import com.example.shop.domain.vo.AddressVO;
import com.example.shop.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收货地址 REST 控制器。
 */
@RestController
@RequestMapping("/api/addresses")
@Tag(name = "收货地址", description = "当前用户收货地址管理接口")
public class AddressController {

    private final AddressService addressService;

    /**
     * 创建收货地址控制器。
     *
     * @param addressService 收货地址服务
     */
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 查询当前用户地址列表。
     *
     * @return 地址列表
     */
    @GetMapping
    @Operation(summary = "查询收货地址", description = "查询当前用户的全部收货地址")
    public ApiResponse<List<AddressVO>> listAddresses() {
        return ApiResponse.success(addressService.listAddresses());
    }

    /**
     * 新增当前用户地址。
     *
     * @param request 地址保存请求
     * @return 新增地址
     */
    @PostMapping
    @Operation(summary = "新增收货地址", description = "为当前用户新增一个收货地址")
    public ApiResponse<AddressVO> addAddress(@Valid @RequestBody AddressSaveRequest request) {
        return ApiResponse.success("新增地址成功", addressService.addAddress(request));
    }

    /**
     * 更新当前用户地址。
     *
     * @param id 地址 ID
     * @param request 地址保存请求
     * @return 更新地址
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新收货地址", description = "更新当前用户的指定收货地址")
    public ApiResponse<AddressVO> updateAddress(@PathVariable Long id,
                                                @Valid @RequestBody AddressSaveRequest request) {
        return ApiResponse.success("更新地址成功", addressService.updateAddress(id, request));
    }

    /**
     * 删除当前用户地址。
     *
     * @param id 地址 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收货地址", description = "删除当前用户的指定收货地址")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.success("删除地址成功", (Void) null);
    }
}
