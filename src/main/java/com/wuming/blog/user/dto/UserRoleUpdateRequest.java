package com.wuming.blog.user.dto;

/**
 * 管理员修改用户角色的请求体。
 *
 * @param role 目标角色，只允许 USER 或 ADMIN
 */
public record UserRoleUpdateRequest(
        String role
) {
}
