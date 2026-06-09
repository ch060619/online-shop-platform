package com.example.shop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.entity.User;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AuthServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void should_returnLoginResult_when_passwordCorrect() {
        when(userMapper.findByUsername("demo")).thenReturn(user());
        when(tokenService.issueToken(1L)).thenReturn("token");

        assertThat(authService.login(loginRequest("demo", "demo123")).getToken()).isEqualTo("token");
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

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("demo");
        user.setPassword("demo123");
        user.setNickname("演示用户");
        return user;
    }
}
