package com.example.shop.interceptor;

import com.example.shop.common.TokenClaims;
import com.example.shop.common.TokenService;
import com.example.shop.common.UserContext;
import com.example.shop.common.UserRole;
import com.example.shop.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Bearer Token 登录认证拦截器。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PRODUCTS_PATH = "/api/products";

    private final TokenService tokenService;

    /**
     * 创建认证拦截器。
     *
     * @param tokenService 令牌服务
     */
    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 校验请求头中的 Bearer Token，并写入当前用户上下文。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @return 是否继续处理请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(401, "请先登录");
        }
        TokenClaims claims = tokenService.parseToken(authorization.substring(BEARER_PREFIX.length()));
        if (requiresAdmin(request) && !UserRole.ADMIN.name().equals(claims.role())) {
            throw new BusinessException(403, "无权访问管理端接口");
        }
        UserContext.setCurrentUser(claims.userId(), claims.role());
        return true;
    }

    /**
     * 请求结束后清理用户上下文。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @param ex 请求异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private boolean requiresAdmin(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith(PRODUCTS_PATH)
                && !"GET".equalsIgnoreCase(request.getMethod())
                && !"OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
