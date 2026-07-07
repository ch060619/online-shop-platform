package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shop.common.TokenService;
import com.example.shop.common.UserContext;
import com.example.shop.domain.dto.ChangePasswordRequest;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.dto.RegisterRequest;
import com.example.shop.domain.entity.RefreshToken;
import com.example.shop.domain.entity.User;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.OrderMapper;
import com.example.shop.repository.mapper.RefreshTokenMapper;
import com.example.shop.repository.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
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
    private OrderMapper orderMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userMapper, refreshTokenMapper, orderMapper, tokenService, passwordEncoder, 604800L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
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
    void should_registerUser_when_usernameAvailable() {
        when(userMapper.findByUsername("new_user")).thenReturn(null);
        when(passwordEncoder.encode("new12345")).thenReturn("encoded-new");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(9L);
            return 1;
        });
        when(tokenService.issueAccessToken(9L, "USER")).thenReturn("access-token");
        when(tokenService.getExpireSeconds()).thenReturn(7200L);
        when(refreshTokenMapper.insert(any(RefreshToken.class))).thenReturn(1);

        assertThat(authService.register(registerRequest()).getUsername()).isEqualTo("new_user");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void should_throwException_when_registerUsernameExists() {
        when(userMapper.findByUsername("new_user")).thenReturn(user());

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
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

    @Test
    void should_returnProfile_when_userExists() {
        UserContext.setCurrentUser(1L, "USER");
        User user = user();
        user.setPoints(120);
        when(userMapper.findById(1L)).thenReturn(user);
        when(orderMapper.countByUserId(1L)).thenReturn(3);

        assertThat(authService.profile().getPoints()).isEqualTo(120);
        assertThat(authService.profile().getOrderCount()).isEqualTo(3);
    }

    @Test
    void should_changePassword_when_oldPasswordCorrect() {
        UserContext.setCurrentUser(1L, "USER");
        when(userMapper.findById(1L)).thenReturn(user());
        when(passwordEncoder.matches("old-password", "encoded-demo")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "encoded-demo")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        authService.changePassword(changePasswordRequest());

        verify(userMapper).updatePassword(1L, "encoded-new");
        verify(refreshTokenMapper).revokeByUserId(1L);
    }

    @Test
    void should_throwException_when_oldPasswordWrong() {
        UserContext.setCurrentUser(1L, "USER");
        when(userMapper.findById(1L)).thenReturn(user());

        assertThatThrownBy(() -> authService.changePassword(changePasswordRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原密码错误");
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

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new_user");
        request.setPassword("new12345");
        request.setNickname("新用户");
        request.setPhone("13700000000");
        return request;
    }

    private ChangePasswordRequest changePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old-password");
        request.setNewPassword("new-password");
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
