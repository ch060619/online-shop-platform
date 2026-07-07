package com.example.shop.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.common.TokenClaims;
import com.example.shop.domain.dto.ChangePasswordRequest;
import com.example.shop.domain.vo.UserProfileVO;
import com.example.shop.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UserController 切片测试。
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnProfile_when_userAuthenticated() throws Exception {
        mockAuthenticatedUser();
        when(authService.profile()).thenReturn(profileVO());

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("demo"))
                .andExpect(jsonPath("$.data.points").value(100));
    }

    @Test
    void should_changePassword_when_requestValid() throws Exception {
        mockAuthenticatedUser();
        doNothing().when(authService).changePassword(ArgumentMatchers.any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"demo123\",\"newPassword\":\"new12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"));
    }

    @Test
    void should_return400_when_newPasswordInvalid() throws Exception {
        mockAuthenticatedUser();
        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"demo123\",\"newPassword\":\"123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private UserProfileVO profileVO() {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(1L);
        vo.setUsername("demo");
        vo.setNickname("演示用户");
        vo.setPhone("13800000000");
        vo.setRole("USER");
        vo.setPoints(100);
        vo.setOrderCount(2);
        return vo;
    }

    private void mockAuthenticatedUser() {
        when(tokenService.parseToken("token")).thenReturn(new TokenClaims(1L, "USER", 9999999999L));
    }
}
