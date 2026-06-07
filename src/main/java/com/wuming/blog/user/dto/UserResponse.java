package com.wuming.blog.user.dto;

import com.wuming.blog.user.entity.User;

import java.time.LocalDateTime;

/**
 * 用户响应数据，不包含密码字段，避免泄露密码密文。
 *
 * @param id 用户 ID
 * @param username 用户名
 * @param role 用户角色
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record UserResponse(
        Long id,
        String username,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将用户实体转换为接口响应对象。
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
