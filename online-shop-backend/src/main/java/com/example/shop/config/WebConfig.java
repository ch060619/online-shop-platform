package com.example.shop.config;

import com.example.shop.interceptor.AuthInterceptor;
import com.example.shop.interceptor.RequestLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;
    private final AuthInterceptor authInterceptor;

    /**
     * 创建 Web MVC 配置。
     *
     * @param requestLogInterceptor 请求日志拦截器
     * @param authInterceptor 登录认证拦截器
     */
    public WebConfig(RequestLogInterceptor requestLogInterceptor, AuthInterceptor authInterceptor) {
        this.requestLogInterceptor = requestLogInterceptor;
        this.authInterceptor = authInterceptor;
    }

    /**
     * 配置跨域规则。
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册请求拦截器。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/cart", "/api/cart/**", "/api/orders", "/api/orders/**",
                        "/api/products/add", "/api/products/update/**", "/api/products/delete/**")
                .excludePathPatterns("/api/auth/**");
    }
}
