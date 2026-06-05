package com.wuming.blog.user.exception;

/**
 * 用户不存在异常，根据用户 ID 查询不到数据时抛出。
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
