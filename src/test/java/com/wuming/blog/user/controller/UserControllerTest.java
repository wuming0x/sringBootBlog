package com.wuming.blog.user.controller;

import com.wuming.blog.user.dto.UserLoginResponse;
import com.wuming.blog.user.dto.UserRoleUpdateRequest;
import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.entity.UserRole;
import com.wuming.blog.user.exception.InvalidLoginException;
import com.wuming.blog.user.exception.UnauthorizedException;
import com.wuming.blog.user.exception.UserExceptionHandler;
import com.wuming.blog.user.exception.UserPermissionException;
import com.wuming.blog.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .andExpect(jsonPath("$.role").value("USER"))
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
        when(userService.login(any())).thenReturn(new UserLoginResponse(1L, "alice", "USER", "jwt-token"));

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
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").value("jwt-token"))
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
    /**
     * 管理员查询用户列表时应返回不含密码的用户信息。
     */
    @Test
    void getByIdShouldReturnUserForAdmin() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.getByIdForAdmin(1L, "Bearer admin-token")).thenReturn(buildUser());

        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    /**
     * 缺少 token 按 ID 查询用户时应返回 401。
     */
    @Test
    void getByIdShouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.getByIdForAdmin(1L, null)).thenThrow(new UnauthorizedException("未登录或登录已失效"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    /**
     * 普通用户按 ID 查询用户时应返回 403。
     */
    @Test
    void getByIdShouldReturnForbiddenForNormalUser() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.getByIdForAdmin(1L, "Bearer user-token")).thenThrow(new UserPermissionException("只有管理员可以执行该操作"));

        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("只有管理员可以执行该操作"));
    }

    /**
     * 管理员查询用户列表时应返回不含密码的用户信息。
     */
    @Test
    void listUsersShouldReturnUsersForAdmin() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.listUsers("Bearer admin-token")).thenReturn(List.of(buildUser()));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    /**
     * 缺少 token 访问用户列表时应返回 401。
     */
    @Test
    void listUsersShouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.listUsers(null)).thenThrow(new UnauthorizedException("未登录或登录已失效"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    /**
     * 普通用户访问用户列表时应返回 403。
     */
    @Test
    void listUsersShouldReturnForbiddenForNormalUser() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        when(userService.listUsers("Bearer user-token")).thenThrow(new UserPermissionException("只有管理员可以执行该操作"));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("只有管理员可以执行该操作"));
    }

    /**
     * 管理员修改其他用户角色时应返回更新后的用户信息。
     */
    @Test
    void updateRoleShouldReturnUpdatedUser() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);
        User user = buildUser();
        user.setRole(UserRole.ADMIN);
        when(userService.updateRole(eq(1L), eq("Bearer admin-token"), any(UserRoleUpdateRequest.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/1/role")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * 管理员删除其他用户时应返回 204。
     */
    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        UserService userService = mock(UserService.class);
        MockMvc mockMvc = buildMockMvc(userService);

        mockMvc.perform(delete("/api/users/2")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(2L, "Bearer admin-token");
    }

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
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.of(2026, 6, 5, 20, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 6, 5, 20, 0));
        return user;
    }
}
