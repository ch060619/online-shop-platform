package com.example.shop.service;

import com.example.shop.domain.dto.ChangePasswordRequest;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.dto.RegisterRequest;
import com.example.shop.domain.vo.UserProfileVO;
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
     * 用户注册。
     *
     * @param request 注册请求
     * @return 登录结果
     */
    LoginVO register(RegisterRequest request);

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新令牌请求
     * @return 新的登录令牌结果
     */
    LoginVO refresh(RefreshTokenRequest request);

    /**
     * 查询当前用户个人中心概览。
     *
     * @return 个人中心概览
     */
    UserProfileVO profile();

    /**
     * 修改当前用户登录密码。
     *
     * @param request 修改密码请求
     */
    void changePassword(ChangePasswordRequest request);
}
