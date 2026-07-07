package com.example.shop.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.common.TokenClaims;
import com.example.shop.domain.dto.AddressSaveRequest;
import com.example.shop.domain.vo.AddressVO;
import com.example.shop.service.AddressService;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AddressController 切片测试。
 */
@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnAddresses_when_userAuthenticated() throws Exception {
        mockAuthenticatedUser();
        when(addressService.listAddresses()).thenReturn(Collections.singletonList(addressVO()));

        mockMvc.perform(get("/api/addresses").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].receiverName").value("张三"));
    }

    @Test
    void should_addAddress_when_requestValid() throws Exception {
        mockAuthenticatedUser();
        when(addressService.addAddress(ArgumentMatchers.any(AddressSaveRequest.class))).thenReturn(addressVO());

        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("新增地址成功"))
                .andExpect(jsonPath("$.data.defaultAddress").value(true));
    }

    @Test
    void should_updateAddress_when_requestValid() throws Exception {
        mockAuthenticatedUser();
        when(addressService.updateAddress(ArgumentMatchers.eq(1L),
                ArgumentMatchers.any(AddressSaveRequest.class))).thenReturn(addressVO());

        mockMvc.perform(put("/api/addresses/1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAddressJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("更新地址成功"));
    }

    @Test
    void should_deleteAddress_when_addressExists() throws Exception {
        mockAuthenticatedUser();
        doNothing().when(addressService).deleteAddress(1L);

        mockMvc.perform(delete("/api/addresses/1").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除地址成功"));
    }

    @Test
    void should_return400_when_phoneInvalid() throws Exception {
        mockAuthenticatedUser();
        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"张三\",\"receiverPhone\":\"100\","
                                + "\"receiverAddress\":\"上海市\",\"defaultAddress\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private String validAddressJson() {
        return "{\"receiverName\":\"张三\",\"receiverPhone\":\"13800000000\","
                + "\"receiverAddress\":\"上海市浦东新区\",\"defaultAddress\":true}";
    }

    private AddressVO addressVO() {
        AddressVO vo = new AddressVO();
        vo.setId(1L);
        vo.setReceiverName("张三");
        vo.setReceiverPhone("13800000000");
        vo.setReceiverAddress("上海市浦东新区");
        vo.setDefaultAddress(true);
        return vo;
    }

    private void mockAuthenticatedUser() {
        when(tokenService.parseToken("token")).thenReturn(new TokenClaims(1L, "USER", 9999999999L));
    }
}
