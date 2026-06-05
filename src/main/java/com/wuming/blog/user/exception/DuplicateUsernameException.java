package com.wuming.blog.user.exception;

/**
 * 用户名重复异常，注册时用户名已存在会抛出该异常。
 */
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
