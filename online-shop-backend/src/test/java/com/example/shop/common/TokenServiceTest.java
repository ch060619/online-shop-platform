package com.example.shop.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.exception.BusinessException;
import org.junit.jupiter.api.Test;

/**
 * TokenService 单元测试。
 */
class TokenServiceTest {

    @Test
    void should_parseUserId_when_tokenValid() {
        TokenService tokenService = new TokenService("test-secret", 60);
        String token = tokenService.issueToken(7L);

        assertThat(tokenService.parseUserId(token)).isEqualTo(7L);
        assertThat(tokenService.parseToken(token).role()).isEqualTo("USER");
    }

    @Test
    void should_parseToken_when_roleProvided() {
        TokenService tokenService = new TokenService("test-secret", 60);
        String token = tokenService.issueAccessToken(7L, "ADMIN");

        assertThat(tokenService.parseToken(token).role()).isEqualTo("ADMIN");
    }

    @Test
    void should_throwException_when_tokenTampered() {
        TokenService tokenService = new TokenService("test-secret", 60);
        String token = tokenService.issueToken(7L) + "x";

        assertThatThrownBy(() -> tokenService.parseUserId(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("登录令牌无效");
    }

    @Test
    void should_throwException_when_tokenExpired() {
        TokenService tokenService = new TokenService("test-secret", -1);
        String token = tokenService.issueToken(7L);

        assertThatThrownBy(() -> tokenService.parseUserId(token))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("登录已过期");
    }
}
