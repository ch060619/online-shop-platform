package com.example.shop.service;

import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.vo.LoginVO;

/**
 * 用户认证服务。
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    LoginVO login(LoginRequest request);

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新令牌请求
     * @return 新的登录令牌结果
     */
    LoginVO refresh(RefreshTokenRequest request);
}
