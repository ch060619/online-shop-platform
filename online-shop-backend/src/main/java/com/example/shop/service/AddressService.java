package com.example.shop.service;

import com.example.shop.domain.dto.AddressSaveRequest;
import com.example.shop.domain.vo.AddressVO;
import java.util.List;

/**
 * 收货地址服务接口。
 */
public interface AddressService {

    /**
     * 查询当前用户地址列表。
     *
     * @return 地址列表
     */
    List<AddressVO> listAddresses();

    /**
     * 新增当前用户地址。
     *
     * @param request 地址保存请求
     * @return 新增地址
     */
    AddressVO addAddress(AddressSaveRequest request);

    /**
     * 更新当前用户地址。
     *
     * @param id 地址 ID
     * @param request 地址保存请求
     * @return 更新后地址
     */
    AddressVO updateAddress(Long id, AddressSaveRequest request);

    /**
     * 删除当前用户地址。
     *
     * @param id 地址 ID
     */
    void deleteAddress(Long id);
}
