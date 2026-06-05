package com.wuming.blog.user.exception;

/**
 * 登录失败异常，用户名不存在或密码错误时抛出。
 */
public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException(String message) {
        super(message);
    }
}
