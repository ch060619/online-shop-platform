package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.dto.RefreshTokenRequest;
import com.example.shop.domain.dto.RegisterRequest;
import com.example.shop.domain.vo.LoginVO;
import com.example.shop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证 REST 控制器。
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户登录、访问令牌和刷新令牌接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 创建用户认证控制器。
     *
     * @param authService 用户认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验用户名密码，成功后返回 access token 与 refresh token")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 注册后的登录结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建普通用户并返回登录令牌，密码使用 BCrypt 加盐哈希存储")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新令牌请求
     * @return 登录结果
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用有效 refresh token 轮换新的 access token 与 refresh token")
    public ApiResponse<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("刷新令牌成功", authService.refresh(request));
    }
}
