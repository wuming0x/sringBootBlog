package com.wuming.blog.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库 users 表。
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * 用户主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，要求唯一。
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 用户密码，只保存 BCrypt 加密后的密文。
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户角色，默认为普通用户。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private UserRole role = UserRole.USER;

    /**
     * 用户创建时间。
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 用户最后更新时间。
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 新增用户前自动填充创建时间和更新时间。
     */
    @PrePersist
    void prePersist() {
        if (this.role == null) {
            this.role = UserRole.USER;
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 更新用户前自动刷新更新时间。
     */
    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
