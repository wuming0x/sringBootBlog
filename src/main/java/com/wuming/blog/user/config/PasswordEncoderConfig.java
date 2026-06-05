package com.wuming.blog.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置，提供 BCrypt 密码加密器。
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 创建 BCrypt PasswordEncoder，用于用户注册时加密密码。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
