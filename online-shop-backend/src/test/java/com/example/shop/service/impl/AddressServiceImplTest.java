package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.AddressSaveRequest;
import com.example.shop.domain.entity.UserAddress;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.UserAddressMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AddressServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private UserAddressMapper userAddressMapper;

    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);
        addressService = new AddressServiceImpl(userAddressMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_addDefaultAddress_when_firstAddressCreated() {
        when(userAddressMapper.findDefaultByUserId(1L)).thenReturn(null);
        when(userAddressMapper.insert(any(UserAddress.class))).thenAnswer(invocation -> {
            UserAddress address = invocation.getArgument(0);
            address.setId(10L);
            return 1;
        });
        when(userAddressMapper.findByIdAndUserId(10L, 1L)).thenReturn(address(true));

        assertThat(addressService.addAddress(request(false)).getDefaultAddress()).isTrue();
        verify(userAddressMapper).clearDefault(1L);
    }

    @Test
    void should_addNonDefaultAddress_when_defaultAddressExists() {
        when(userAddressMapper.findDefaultByUserId(1L)).thenReturn(address(true));
        when(userAddressMapper.insert(any(UserAddress.class))).thenAnswer(invocation -> {
            UserAddress address = invocation.getArgument(0);
            address.setId(11L);
            return 1;
        });
        when(userAddressMapper.findByIdAndUserId(11L, 1L)).thenReturn(address(false));

        assertThat(addressService.addAddress(request(false)).getDefaultAddress()).isFalse();
        verify(userAddressMapper, never()).clearDefault(1L);
    }

    @Test
    void should_updateAddress_when_addressExists() {
        when(userAddressMapper.findByIdAndUserId(10L, 1L)).thenReturn(address(false), address(true));

        assertThat(addressService.updateAddress(10L, request(true)).getDefaultAddress()).isTrue();
        verify(userAddressMapper).clearDefault(1L);
        verify(userAddressMapper).update(any(UserAddress.class));
    }

    @Test
    void should_throwException_when_updateMissingAddress() {
        when(userAddressMapper.findByIdAndUserId(99L, 1L)).thenReturn(null);

        assertThatThrownBy(() -> addressService.updateAddress(99L, request(true)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("收货地址不存在");
    }

    @Test
    void should_deleteAddress_when_addressExists() {
        when(userAddressMapper.deleteByIdAndUserId(10L, 1L)).thenReturn(1);

        addressService.deleteAddress(10L);

        verify(userAddressMapper).deleteByIdAndUserId(10L, 1L);
    }

    @Test
    void should_throwException_when_deleteMissingAddress() {
        when(userAddressMapper.deleteByIdAndUserId(99L, 1L)).thenReturn(0);

        assertThatThrownBy(() -> addressService.deleteAddress(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("收货地址不存在");
    }

    private AddressSaveRequest request(boolean defaultAddress) {
        AddressSaveRequest request = new AddressSaveRequest();
        request.setReceiverName("张三");
        request.setReceiverPhone("13800000000");
        request.setReceiverAddress("上海市浦东新区");
        request.setDefaultAddress(defaultAddress);
        return request;
    }

    private UserAddress address(boolean defaultAddress) {
        UserAddress address = new UserAddress();
        address.setId(10L);
        address.setUserId(1L);
        address.setReceiverName("张三");
        address.setReceiverPhone("13800000000");
        address.setReceiverAddress("上海市浦东新区");
        address.setDefaultAddress(defaultAddress);
        return address;
    }
}
