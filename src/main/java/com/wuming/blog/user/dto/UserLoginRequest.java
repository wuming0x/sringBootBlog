package com.wuming.blog.user.dto;

/**
 * 用户登录请求参数。
 *
 * @param username 用户名
 * @param password 明文密码，服务层会使用 BCrypt 进行匹配校验
 */
public record UserLoginRequest(
        String username,
        String password
) {
}
