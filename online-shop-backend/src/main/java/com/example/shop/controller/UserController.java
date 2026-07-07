package com.example.shop.controller;

import com.example.shop.common.ApiResponse;
import com.example.shop.domain.dto.ChangePasswordRequest;
import com.example.shop.domain.vo.UserProfileVO;
import com.example.shop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户个人中心 REST 控制器。
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "个人中心", description = "当前用户资料、积分和密码接口")
public class UserController {

    private final AuthService authService;

    /**
     * 创建个人中心控制器。
     *
     * @param authService 用户认证服务
     */
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 查询当前用户资料。
     *
     * @return 当前用户资料
     */
    @GetMapping("/me")
    @Operation(summary = "查询个人中心", description = "查询当前用户资料、积分和订单数量")
    public ApiResponse<UserProfileVO> profile() {
        return ApiResponse.success(authService.profile());
    }

    /**
     * 修改当前用户登录密码。
     *
     * @param request 修改密码请求
     * @return 修改结果
     */
    @PutMapping("/me/password")
    @Operation(summary = "修改登录密码", description = "校验原密码后使用 BCrypt 保存新密码")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("密码修改成功，请重新登录", (Void) null);
    }
}
