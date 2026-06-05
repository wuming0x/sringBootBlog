package com.wuming.blog.user.exception;

/**
 * 用户请求参数异常，例如用户名或密码为空。
 */
public class InvalidUserRequestException extends RuntimeException {

    public InvalidUserRequestException(String message) {
        super(message);
    }
}
