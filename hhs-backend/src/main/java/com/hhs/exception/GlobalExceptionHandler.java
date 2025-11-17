package com.hhs.exception;

import com.hhs.common.Result;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, ValidationException.class})
    public Result<Void> handleValidationException(Exception ex) {
        log.warn("参数校验异常", ex);
        String message;
        if (ex instanceof MethodArgumentNotValidException e) {
            message = e.getBindingResult().getFieldError() != null
                    ? e.getBindingResult().getFieldError().getDefaultMessage()
                    : "参数校验失败";
        } else {
            message = ex.getMessage();
        }
        return Result.failure(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败", ex);
        return Result.failure(400, "请求体格式错误");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("资源未找到: {}", ex.getResourcePath());
        // 对于静态资源（如头像）不存在的情况，返回404但不记录为错误
        return Result.failure(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return Result.failure(500, "系统异常，请稍后重试");
    }
}
