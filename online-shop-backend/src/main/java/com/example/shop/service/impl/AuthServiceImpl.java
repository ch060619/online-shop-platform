package com.example.shop.service.impl;

import com.example.shop.common.TokenService;
import com.example.shop.domain.dto.LoginRequest;
import com.example.shop.domain.entity.User;
import com.example.shop.domain.vo.LoginVO;
import com.example.shop.exception.BusinessException;
import com.example.shop.repository.mapper.UserMapper;
import com.example.shop.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final TokenService tokenService;

    /**
     * 创建用户认证服务实现。
     *
     * @param userMapper 用户 Mapper
     * @param tokenService 令牌服务
     */
    public AuthServiceImpl(UserMapper userMapper, TokenService tokenService) {
        this.userMapper = userMapper;
        this.tokenService = tokenService;
    }

    /**
     * 校验用户名密码并签发登录令牌。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(tokenService.issueToken(user.getId()));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
