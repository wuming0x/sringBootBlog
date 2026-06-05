package com.wuming.blog.user.exception;

import com.wuming.blog.user.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 用户模块统一异常处理器，将业务异常转换为 REST 错误响应。
 */
@RestControllerAdvice
public class UserExceptionHandler {

    /**
     * 处理用户名重复异常，返回 400。
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUsername(DuplicateUsernameException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理用户请求参数异常，返回 400。
     */
    @ExceptionHandler(InvalidUserRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidUserRequest(InvalidUserRequestException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理登录失败异常，返回 400。
     */
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLogin(InvalidLoginException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理用户不存在异常，返回 404。
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }
}
