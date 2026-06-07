package com.wuming.blog.comment.exception;

import com.wuming.blog.user.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 评论模块统一异常处理器，将业务异常转换为 REST 错误响应。
 */
@RestControllerAdvice
public class CommentExceptionHandler {

    /**
     * 处理评论请求参数异常，返回 400。
     */
    @ExceptionHandler(InvalidCommentRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCommentRequest(InvalidCommentRequestException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理评论不存在异常，返回 404。
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCommentNotFound(CommentNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }

    /**
     * 处理评论权限异常，返回 403。
     */
    @ExceptionHandler(CommentPermissionException.class)
    public ResponseEntity<ApiErrorResponse> handleCommentPermission(CommentPermissionException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(exception.getMessage()));
    }
}
