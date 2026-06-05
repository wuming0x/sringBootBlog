package com.wuming.blog.user.controller;

import com.wuming.blog.user.dto.UserLoginResponse;
import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.exception.InvalidLoginException;
import com.wuming.blog.user.exception.UserExceptionHandler;
import com.wuming.blog.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户接口测试，使用 MockMvc 验证 REST 响应。
 */
class UserControllerTest {

    /**
     * 注册成功时应返回 201，且响应中不包含 password 字段。
     */
    @Test
    void registerShouldReturnCreatedUserWithoutPassword() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        User user = buildUser();
        when(userService.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(content().string(not(containsString("encoded-password"))));
    }

    /**
     * 登录成功时应返回用户信息和 token 字段，且不返回 password 字段。
     */
    @Test
    void loginShouldReturnUserInfoWithTokenField() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.login(any())).thenReturn(new UserLoginResponse(1L, "alice", null));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.token").value((String) null))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    /**
     * 登录失败时应返回 400 和统一错误提示。
     */
    @Test
    void loginShouldReturnBadRequestWhenCredentialsInvalid() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.login(any())).thenThrow(new InvalidLoginException("用户名或密码错误"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 构建带统一异常处理器的 MockMvc。
     */
    private MockMvc buildMockMvc(UserService userService) {
        return MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new UserExceptionHandler())
                .build();
    }

    /**
     * 构造接口响应使用的用户实体。
     */
    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setCreatedAt(LocalDateTime.of(2026, 6, 5, 20, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 6, 5, 20, 0));
        return user;
    }
}
