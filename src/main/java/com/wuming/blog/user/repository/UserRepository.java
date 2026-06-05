package com.wuming.blog.user.repository;

import com.wuming.blog.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问层，负责 users 表的基础 CRUD 和用户名查询。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 判断用户名是否已经存在，用于注册时查重。
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名查询用户。
     */
    Optional<User> findByUsername(String username);
}
