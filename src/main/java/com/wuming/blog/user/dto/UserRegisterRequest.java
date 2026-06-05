package com.wuming.blog.user.dto;

/**
 * 用户注册请求参数。
 *
 * @param username 用户名
 * @param password 明文密码，服务层会使用 BCrypt 加密后保存
 */
public record UserRegisterRequest(
        String username,
        String password
) {
}
