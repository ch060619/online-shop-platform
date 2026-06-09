package com.example.shop.common;

import com.example.shop.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 登录令牌签发与校验服务。
 */
@Component
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_SEPARATOR = ".";

    private final String secret;
    private final long expireSeconds;

    /**
     * 创建令牌服务。
     *
     * @param secret 令牌签名密钥
     * @param expireSeconds 令牌有效秒数
     */
    public TokenService(@Value("${shop.auth.token-secret:online-shop-demo-secret}") String secret,
                        @Value("${shop.auth.token-expire-seconds:7200}") long expireSeconds) {
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    /**
     * 为指定用户签发令牌。
     *
     * @param userId 用户 ID
     * @return 登录令牌
     */
    public String issueToken(Long userId) {
        long expiresAt = Instant.now().getEpochSecond() + expireSeconds;
        String payload = userId + ":" + expiresAt;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + TOKEN_SEPARATOR + sign(encodedPayload);
    }

    /**
     * 解析并校验令牌。
     *
     * @param token 登录令牌
     * @return 用户 ID
     */
    public Long parseUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            throw new BusinessException(401, "登录令牌无效");
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 2) {
                throw new BusinessException(401, "登录令牌无效");
            }
            long expiresAt = Long.parseLong(payloadParts[1]);
            if (expiresAt < Instant.now().getEpochSecond()) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }
            return Long.valueOf(payloadParts[0]);
        }
        catch (IllegalArgumentException exception) {
            throw new BusinessException(401, "登录令牌无效");
        }
    }

    private String sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return encode(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception exception) {
            throw new IllegalStateException("无法生成登录令牌签名", exception);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
