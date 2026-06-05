package com.wuming.blog.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置，开放当前用户模块接口，避免被 Spring Security 默认登录拦截。
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤链，关闭 CSRF 并允许访问用户模块 API。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/users/**").permitAll()
                        .anyRequest().permitAll()
                )
                .build();
    }
}
