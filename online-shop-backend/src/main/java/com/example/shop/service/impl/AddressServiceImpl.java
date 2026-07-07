package com.example.shop.service.impl;

import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.AddressSaveRequest;
import com.example.shop.domain.entity.UserAddress;
import com.example.shop.domain.vo.AddressVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.UserAddressMapper;
import com.example.shop.service.AddressService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收货地址服务实现。
 */
@Service
public class AddressServiceImpl implements AddressService {

    private final UserAddressMapper userAddressMapper;

    /**
     * 创建收货地址服务实现。
     *
     * @param userAddressMapper 收货地址 Mapper
     */
    public AddressServiceImpl(UserAddressMapper userAddressMapper) {
        this.userAddressMapper = userAddressMapper;
    }

    /**
     * 查询当前用户地址列表。
     *
     * @return 地址列表
     */
    @Override
    public List<AddressVO> listAddresses() {
        return userAddressMapper.findByUserId(UserContext.getCurrentUserId()).stream()
                .map(this::toAddressVO)
                .collect(Collectors.toList());
    }

    /**
     * 新增当前用户地址。
     *
     * @param request 地址保存请求
     * @return 新增地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO addAddress(AddressSaveRequest request) {
        Long userId = UserContext.getCurrentUserId();
        boolean defaultAddress = Boolean.TRUE.equals(request.getDefaultAddress())
                || userAddressMapper.findDefaultByUserId(userId) == null;
        if (defaultAddress) {
            userAddressMapper.clearDefault(userId);
        }
        UserAddress address = toAddress(userId, null, request, defaultAddress);
        userAddressMapper.insert(address);
        return toAddressVO(userAddressMapper.findByIdAndUserId(address.getId(), userId));
    }

    /**
     * 更新当前用户地址。
     *
     * @param id 地址 ID
     * @param request 地址保存请求
     * @return 更新后地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(Long id, AddressSaveRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userAddressMapper.findByIdAndUserId(id, userId) == null) {
            throw new BusinessException(404, "收货地址不存在");
        }
        boolean defaultAddress = Boolean.TRUE.equals(request.getDefaultAddress());
        if (defaultAddress) {
            userAddressMapper.clearDefault(userId);
        }
        UserAddress address = toAddress(userId, id, request, defaultAddress);
        userAddressMapper.update(address);
        return toAddressVO(userAddressMapper.findByIdAndUserId(id, userId));
    }

    /**
     * 删除当前用户地址。
     *
     * @param id 地址 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id) {
        Long userId = UserContext.getCurrentUserId();
        int affected = userAddressMapper.deleteByIdAndUserId(id, userId);
        if (affected == 0) {
            throw new BusinessException(404, "收货地址不存在");
        }
    }

    private UserAddress toAddress(Long userId, Long id, AddressSaveRequest request, boolean defaultAddress) {
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName().trim());
        address.setReceiverPhone(request.getReceiverPhone().trim());
        address.setReceiverAddress(request.getReceiverAddress().trim());
        address.setDefaultAddress(defaultAddress);
        return address;
    }

    private AddressVO toAddressVO(UserAddress address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setReceiverAddress(address.getReceiverAddress());
        vo.setDefaultAddress(Boolean.TRUE.equals(address.getDefaultAddress()));
        return vo;
    }
}
