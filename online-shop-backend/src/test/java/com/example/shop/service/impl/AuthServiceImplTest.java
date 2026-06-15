package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.entity.RefreshToken;
import com.example.shop.domain.entity.User;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.RefreshTokenMapper;
import com.example.shop.repository.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AuthServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, refreshTokenMapper, tokenService, passwordEncoder, 604800L);
    }

    @Test
    void should_returnLoginResult_when_passwordCorrect() {
        when(userMapper.findByUsername("demo")).thenReturn(user());
        when(passwordEncoder.matches("demo123", "encoded-demo")).thenReturn(true);
        when(tokenService.issueAccessToken(1L, "USER")).thenReturn("access-token");
        when(tokenService.getExpireSeconds()).thenReturn(7200L);
        when(refreshTokenMapper.insert(any(RefreshToken.class))).thenReturn(1);

        assertThat(authService.login(loginRequest("demo", "demo123")).getToken()).isEqualTo("access-token");
    }

    @Test
    void should_throwException_when_passwordWrong() {
        when(userMapper.findByUsername("demo")).thenReturn(user());

        assertThatThrownBy(() -> authService.login(loginRequest("demo", "bad")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void should_throwException_when_userNotFound() {
        when(userMapper.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> authService.login(loginRequest("missing", "demo123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void should_returnNewTokens_when_refreshTokenValid() {
        when(refreshTokenMapper.findActiveByTokenHash(any(), any())).thenReturn(refreshToken());
        when(userMapper.findById(1L)).thenReturn(user());
        when(tokenService.issueAccessToken(1L, "USER")).thenReturn("new-access-token");
        when(tokenService.getExpireSeconds()).thenReturn(7200L);
        when(refreshTokenMapper.revokeByTokenHash(any())).thenReturn(1);
        when(refreshTokenMapper.insert(any(RefreshToken.class))).thenReturn(1);

        assertThat(authService.refresh(refreshTokenRequest()).getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void should_throwException_when_refreshTokenInvalid() {
        assertThatThrownBy(() -> authService.refresh(refreshTokenRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("刷新令牌无效或已过期");
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private RefreshTokenRequest refreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        return request;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("demo");
        user.setPassword("encoded-demo");
        user.setNickname("演示用户");
        user.setRole("USER");
        return user;
    }

    private RefreshToken refreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setTokenHash("hash");
        refreshToken.setUserId(1L);
        refreshToken.setRevoked(false);
        return refreshToken;
    }
}
