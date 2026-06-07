package com.wuming.blog.user.dto;

import com.wuming.blog.user.entity.User;

/**
 * 用户登录响应数据。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param role 用户角色
 * @param token 登录令牌，后续请求写操作时放入 Authorization Bearer 请求头
 */
public record UserLoginResponse(
        Long id,
        String username,
        String role,
        String token
) {
    /**
     * 将用户实体和 JWT 转换为登录响应对象。
     */
    public static UserLoginResponse from(User user, String token) {
        return new UserLoginResponse(user.getId(), user.getUsername(), user.getRole().name(), token);
    }
}
