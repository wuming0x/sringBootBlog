package com.wuming.blog.article.exception;

import com.wuming.blog.user.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 文章模块统一异常处理器，将业务异常转换为 REST 错误响应。
 */
@RestControllerAdvice
public class ArticleExceptionHandler {

    /**
     * 处理文章请求参数异常，返回 400。
     */
    @ExceptionHandler(InvalidArticleRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArticleRequest(InvalidArticleRequestException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理文章不存在异常，返回 404。
     */
    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleArticleNotFound(ArticleNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理文章权限异常，返回 403。
     */
    @ExceptionHandler(ArticlePermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleArticlePermission(ArticlePermissionException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }
}
