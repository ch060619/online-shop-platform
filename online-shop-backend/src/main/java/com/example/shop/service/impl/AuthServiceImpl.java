package com.example.shop.service.impl;

import com.example.shop.common.TokenService;
import com.example.shop.common.UserRole;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.entity.RefreshToken;
import com.example.shop.domain.entity.User;
import com.example.shop.domain.vo.LoginVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.RefreshTokenMapper;
import com.example.shop.repository.mapper.UserMapper;
import com.example.shop.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final String TOKEN_TYPE = "Bearer";

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshTokenExpireSeconds;

    /**
     * 创建用户认证服务实现。
     *
     * @param userMapper 用户 Mapper
     * @param refreshTokenMapper 刷新令牌 Mapper
     * @param tokenService 令牌服务
     * @param passwordEncoder 密码编码器
     * @param refreshTokenExpireSeconds 刷新令牌有效秒数
     */
    public AuthServiceImpl(UserMapper userMapper,
                           RefreshTokenMapper refreshTokenMapper,
                           TokenService tokenService,
                           PasswordEncoder passwordEncoder,
                           @Value("${shop.auth.refresh-token-expire-seconds:604800}")
                           long refreshTokenExpireSeconds) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }

    /**
     * 校验用户名密码并签发登录令牌。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @Override
    @Transactional
    public LoginVO login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return issueLoginTokens(user);
    }

    /**
     * 使用 refresh token 轮换访问令牌。
     *
     * @param request 刷新令牌请求
     * @return 新的登录结果
     */
    @Override
    @Transactional
    public LoginVO refresh(RefreshTokenRequest request) {
        String tokenHash = hashRefreshToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenMapper.findActiveByTokenHash(tokenHash, LocalDateTime.now());
        if (refreshToken == null) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        User user = userMapper.findById(refreshToken.getUserId());
        if (user == null) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        refreshTokenMapper.revokeByTokenHash(tokenHash);
        return issueLoginTokens(user);
    }

    private LoginVO issueLoginTokens(User user) {
        String role = UserRole.from(user.getRole()).name();
        String accessToken = tokenService.issueAccessToken(user.getId(), role);
        String refreshToken = generateRefreshToken();
        saveRefreshToken(user.getId(), refreshToken);
        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresInSeconds(tokenService.getExpireSeconds());
        vo.setTokenType(TOKEN_TYPE);
        vo.setRole(role);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }

    private void saveRefreshToken(Long userId, String refreshTokenValue) {
        RefreshToken refreshToken = new RefreshToken();
        LocalDateTime now = LocalDateTime.now();
        refreshToken.setTokenHash(hashRefreshToken(refreshTokenValue));
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(now.plusSeconds(refreshTokenExpireSeconds));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(now);
        refreshTokenMapper.insert(refreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        }
        catch (Exception exception) {
            throw new IllegalStateException("无法生成刷新令牌摘要", exception);
        }
    }
}
