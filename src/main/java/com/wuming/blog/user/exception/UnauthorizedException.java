package com.wuming.blog.user.exception;

/**
 * 未认证异常，请求缺少有效登录 token 时抛出。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
