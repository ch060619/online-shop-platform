package com.example.shop.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.vo.LoginVO;
import com.example.shop.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AuthController 切片测试。
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @Test
    void should_returnToken_when_loginValid() throws Exception {
        when(authService.login(ArgumentMatchers.any(LoginRequest.class))).thenReturn(loginVO());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").value("token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void should_return400_when_loginInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void should_returnToken_when_refreshValid() throws Exception {
        when(authService.refresh(ArgumentMatchers.any(RefreshTokenRequest.class))).thenReturn(loginVO());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("刷新令牌成功"))
                .andExpect(jsonPath("$.data.accessToken").value("token"));
    }

    private LoginVO loginVO() {
        LoginVO vo = new LoginVO();
        vo.setToken("token");
        vo.setAccessToken("token");
        vo.setRefreshToken("refresh-token");
        vo.setExpiresInSeconds(7200L);
        vo.setTokenType("Bearer");
        vo.setRole("USER");
        vo.setUserId(1L);
        vo.setUsername("demo");
        vo.setNickname("演示用户");
        return vo;
    }
}
