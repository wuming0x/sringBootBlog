package com.wuming.blog.user.service;

import com.wuming.blog.user.dto.UserLoginRequest;
import com.wuming.blog.user.dto.UserLoginResponse;
import com.wuming.blog.user.dto.UserRegisterRequest;
import com.wuming.blog.user.dto.UserRoleUpdateRequest;
import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.entity.UserRole;
import com.wuming.blog.user.exception.DuplicateUsernameException;
import com.wuming.blog.user.exception.InvalidLoginException;
import com.wuming.blog.user.exception.InvalidUserRequestException;
import com.wuming.blog.user.exception.UnauthorizedException;
import com.wuming.blog.user.exception.UserNotFoundException;
import com.wuming.blog.user.exception.UserPermissionException;
import com.wuming.blog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户业务测试，使用 Mock 隔离数据库访问。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /**
     * 用户数据访问 Mock。
     */
    @Mock
    private UserRepository userRepository;

    /**
     * 真实 BCrypt 密码加密器，用于验证密码加密和匹配。
     */
    private PasswordEncoder passwordEncoder;

    /**
     * 被测试的用户服务。
     */
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, new JwtService("test-secret"));
    }

    /**
     * 注册成功时应加密密码并保存用户。
     */
    @Test
    void registerShouldEncodePasswordAndSaveUser() {
        UserRegisterRequest request = new UserRegisterRequest("alice", "123456");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User savedUser = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();
        assertEquals("alice", savedUser.getUsername());
        assertEquals("alice", userToSave.getUsername());
        assertEquals(UserRole.USER, userToSave.getRole());
        assertNotEquals("123456", userToSave.getPassword());
        assertTrue(passwordEncoder.matches("123456", userToSave.getPassword()));
    }

    /**
     * 用户名为空时应抛出参数异常。
     */
    @Test
    void registerShouldRejectBlankUsername() {
        UserRegisterRequest request = new UserRegisterRequest(" ", "123456");

        InvalidUserRequestException exception = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.register(request)
        );

        assertEquals("用户名不能为空", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * 密码为空时应抛出参数异常。
     */
    @Test
    void registerShouldRejectBlankPassword() {
        UserRegisterRequest request = new UserRegisterRequest("alice", " ");

        InvalidUserRequestException exception = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.register(request)
        );

        assertEquals("密码不能为空", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * 用户名重复时应抛出重复用户名异常，并且不保存用户。
     */
    @Test
    void registerShouldRejectDuplicateUsername() {
        UserRegisterRequest request = new UserRegisterRequest("alice", "123456");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> userService.register(request)
        );

        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * 登录成功时应返回用户信息和非空 JWT token。
     */
    @Test
    void loginShouldReturnUserInfoWithToken() {
        User user = buildUser("alice", "123456");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserLoginResponse response = userService.login(new UserLoginRequest("alice", "123456"));

        assertEquals(1L, response.id());
        assertEquals("alice", response.username());
        assertEquals("USER", response.role());
        assertNotNull(response.token());
    }

    /**
     * 有效 token 应能解析为当前登录用户。
     */
    @Test
    void getCurrentUserShouldReturnUserFromToken() {
        User user = buildUser("alice", "123456");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("alice", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User currentUser = userService.getCurrentUser("Bearer " + loginResponse.token());

        assertEquals(1L, currentUser.getId());
        assertEquals("alice", currentUser.getUsername());
    }

    /**
     * 管理员可以查询用户列表。
     */
    @Test
    void listUsersShouldAllowAdmin() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        userService.listUsers("Bearer " + loginResponse.token());

        verify(userRepository).findAll();
    }

    /**
     * 普通用户不能访问管理员用户列表。
     */
    @Test
    void listUsersShouldRejectNormalUser() {
        User user = buildUser("alice", "123456");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("alice", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserPermissionException exception = assertThrows(
                UserPermissionException.class,
                () -> userService.listUsers("Bearer " + loginResponse.token())
        );

        assertEquals("只有管理员可以执行该操作", exception.getMessage());
        verify(userRepository, never()).findAll();
    }

    /**
     * 缺少 token 时不能访问管理员用户列表。
     */
    @Test
    void listUsersShouldRejectMissingToken() {
        assertThrows(UnauthorizedException.class, () -> userService.listUsers(null));
        verify(userRepository, never()).findAll();
    }

    /**
     * 管理员可以修改其他用户角色。
     */
    @Test
    void updateRoleShouldAllowAdminToChangeOtherUser() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        User user = buildUser(2L, "alice", "123456", UserRole.USER);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        User updatedUser = userService.updateRole(2L, "Bearer " + loginResponse.token(), new UserRoleUpdateRequest("ADMIN"));

        assertEquals(UserRole.ADMIN, updatedUser.getRole());
    }

    /**
     * 管理员不能把自己降级为普通用户，避免失去后台入口。
     */
    @Test
    void updateRoleShouldRejectSelfDemotion() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserPermissionException exception = assertThrows(
                UserPermissionException.class,
                () -> userService.updateRole(1L, "Bearer " + loginResponse.token(), new UserRoleUpdateRequest("USER"))
        );

        assertEquals("管理员不能将自己降级为普通用户", exception.getMessage());
        assertEquals(UserRole.ADMIN, admin.getRole());
    }

    /**
     * 管理员可以删除其他用户。
     */
    @Test
    void deleteUserShouldAllowAdminToDeleteOtherUser() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        User user = buildUser(2L, "alice", "123456", UserRole.USER);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.deleteUser(2L, "Bearer " + loginResponse.token());

        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    /**
     * 管理员不能删除自己的账号，避免系统没有可用后台账号。
     */
    @Test
    void deleteUserShouldRejectSelfDelete() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserPermissionException exception = assertThrows(
                UserPermissionException.class,
                () -> userService.deleteUser(1L, "Bearer " + loginResponse.token())
        );

        assertEquals("管理员不能删除自己的账号", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * 管理员操作不存在的用户时返回用户不存在异常。
     */
    @Test
    void deleteUserShouldRejectUnknownUser() {
        User admin = buildUser(1L, "admin", "123456", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        UserLoginResponse loginResponse = userService.login(new UserLoginRequest("admin", "123456"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L, "Bearer " + loginResponse.token()));
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * 登录用户名为空时应抛出参数异常。
     */
    @Test
    void loginShouldRejectBlankUsername() {
        InvalidUserRequestException exception = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.login(new UserLoginRequest(" ", "123456"))
        );

        assertEquals("用户名不能为空", exception.getMessage());
    }

    /**
     * 登录密码为空时应抛出参数异常。
     */
    @Test
    void loginShouldRejectBlankPassword() {
        InvalidUserRequestException exception = assertThrows(
                InvalidUserRequestException.class,
                () -> userService.login(new UserLoginRequest("alice", " "))
        );

        assertEquals("密码不能为空", exception.getMessage());
    }

    /**
     * 用户不存在时应返回统一登录失败提示。
     */
    @Test
    void loginShouldRejectUnknownUsername() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        InvalidLoginException exception = assertThrows(
                InvalidLoginException.class,
                () -> userService.login(new UserLoginRequest("alice", "123456"))
        );

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    /**
     * 密码错误时应返回统一登录失败提示。
     */
    @Test
    void loginShouldRejectWrongPassword() {
        User user = buildUser("alice", "123456");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        InvalidLoginException exception = assertThrows(
                InvalidLoginException.class,
                () -> userService.login(new UserLoginRequest("alice", "wrong-password"))
        );

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    /**
     * 构造带 BCrypt 密码的用户实体。
     */
    private User buildUser(String username, String rawPassword) {
        return buildUser(1L, username, rawPassword, UserRole.USER);
    }

    /**
     * 构造指定 ID 和角色的测试用户。
     */
    private User buildUser(Long id, String username, String rawPassword, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }
}
