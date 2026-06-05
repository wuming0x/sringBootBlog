package com.wuming.blog.user.dto;

import com.wuming.blog.user.entity.User;

/**
 * 用户登录响应数据。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param token 登录令牌，当前阶段固定返回 null，预留给后续 JWT 升级
 */
public record UserLoginResponse(
        Long id,
        String username,
        String token
) {
    /**
     * 将用户实体转换为登录响应对象。
     */
    public static UserLoginResponse from(User user) {
        return new UserLoginResponse(user.getId(), user.getUsername(), null);
    }
}
