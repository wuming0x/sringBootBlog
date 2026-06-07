package com.wuming.blog.user.entity;

/**
 * 用户角色枚举，用于区分普通用户和管理员。
 */
public enum UserRole {

    /**
     * 普通用户，只能管理自己创建的内容。
     */
    USER,

    /**
     * 管理员，可以管理所有用户的文章和评论。
     */
    ADMIN
}
