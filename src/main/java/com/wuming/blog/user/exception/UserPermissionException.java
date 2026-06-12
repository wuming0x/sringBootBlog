package com.wuming.blog.user.exception;

/**
 * 用户管理权限异常，当前用户无权执行管理员操作时抛出。
 */
public class UserPermissionException extends RuntimeException {

    public UserPermissionException(String message) {
        super(message);
    }
}
