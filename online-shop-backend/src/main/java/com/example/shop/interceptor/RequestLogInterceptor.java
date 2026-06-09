package com.example.shop.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器。
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "requestStartTime";

    /**
     * 请求进入 Controller 前记录访问基础信息。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器
     * @return 是否继续处理请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        LOGGER.info("request method={}, path={}, query={}",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return true;
    }

    /**
     * 请求完成后记录耗时并清理上下文。
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
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        long elapsed = 0L;
        if (startTime instanceof Long) {
            elapsed = System.currentTimeMillis() - (Long) startTime;
        }
        LOGGER.info("response method={}, path={}, status={}, elapsedMs={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed);
    }
}
