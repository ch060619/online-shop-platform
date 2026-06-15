package com.example.shop.repository.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.domain.entity.RefreshToken;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * RefreshTokenMapper 查询测试。
 */
@MybatisTest
@ActiveProfiles("sqlite")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenMapperTest {

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Test
    void should_findActiveToken_when_tokenNotExpired() {
        RefreshToken refreshToken = refreshToken("hash-active", LocalDateTime.now().plusMinutes(5), false);

        refreshTokenMapper.insert(refreshToken);
        RefreshToken found = refreshTokenMapper.findActiveByTokenHash("hash-active", LocalDateTime.now());

        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getRevoked()).isFalse();
    }

    @Test
    void should_returnNull_when_tokenExpired() {
        RefreshToken refreshToken = refreshToken("hash-expired", LocalDateTime.now().minusMinutes(5), false);

        refreshTokenMapper.insert(refreshToken);
        RefreshToken found = refreshTokenMapper.findActiveByTokenHash("hash-expired", LocalDateTime.now());

        assertThat(found).isNull();
    }

    @Test
    void should_revokeToken_when_tokenExists() {
        RefreshToken refreshToken = refreshToken("hash-revoke", LocalDateTime.now().plusMinutes(5), false);
        refreshTokenMapper.insert(refreshToken);

        int updated = refreshTokenMapper.revokeByTokenHash("hash-revoke");
        RefreshToken found = refreshTokenMapper.findActiveByTokenHash("hash-revoke", LocalDateTime.now());

        assertThat(updated).isEqualTo(1);
        assertThat(found).isNull();
    }

    private RefreshToken refreshToken(String tokenHash, LocalDateTime expiresAt, boolean revoked) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUserId(1L);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(revoked);
        refreshToken.setCreatedAt(LocalDateTime.now());
        return refreshToken;
    }
}
