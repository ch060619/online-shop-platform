package com.example.shop.exception;

import com.example.shop.common.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.error(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception) {
        return ApiResponse.error(400, "请求参数校验失败", fieldErrors(exception));
    }

    /**
     * 处理查询参数绑定异常。
     *
     * @param exception 参数绑定异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Map<String, String>> handleBindException(BindException exception) {
        return ApiResponse.error(400, "请求参数校验失败", fieldErrors(exception));
    }

    /**
     * 处理参数类型错误。
     *
     * @param exception 参数类型异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return ApiResponse.error(400, "请求参数类型错误：" + exception.getName());
    }

    /**
     * 处理接口不存在。
     *
     * @param exception 接口不存在异常
     * @return 统一错误响应
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ApiResponse<Void> handleNotFoundException(Exception exception) {
        return ApiResponse.error(404, "接口不存在");
    }

    private Map<String, String> fieldErrors(BindException exception) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    /**
     * 处理未预期异常。
     *
     * @param exception 未预期异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.error(500, "服务器内部错误：" + exception.getMessage());
    }
}
