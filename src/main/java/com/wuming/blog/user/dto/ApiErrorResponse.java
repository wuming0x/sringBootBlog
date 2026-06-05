package com.wuming.blog.user.dto;

import java.time.LocalDateTime;

/**
 * REST API 错误响应对象。
 *
 * @param message 错误提示信息
 * @param timestamp 错误发生时间
 */
public record ApiErrorResponse(
        String message,
        LocalDateTime timestamp
) {
    /**
     * 根据错误提示创建错误响应。
     */
    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(message, LocalDateTime.now());
    }
}
