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
import com.wuming.blog.user.exception.UserNotFoundException;
import com.wuming.blog.user.exception.UserPermissionException;
import com.wuming.blog.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务服务，负责注册、登录、查询和密码加密等业务逻辑。
 */
@Service
public class UserService {

    /**
     * 用户数据访问对象。
     */
    private final UserRepository userRepository;

    /**
     * 密码加密器，用于生成 BCrypt 密码密文。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT 服务，用于生成和解析登录令牌。
     */
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 注册新用户。
     *
     * @param request 注册请求参数
     * @return 保存后的用户实体
     */
    @Transactional
    public User register(UserRegisterRequest request) {
        if (request == null) {
            throw new InvalidUserRequestException("请求参数不能为空");
        }

        String username = normalize(request.username());
        String password = normalize(request.password());

        if (username == null) {
            throw new InvalidUserRequestException("用户名不能为空");
        }
        if (password == null) {
            throw new InvalidUserRequestException("密码不能为空");
        }
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    /**
     * 用户登录，校验用户名和 BCrypt 密码。
     *
     * @param request 登录请求参数
     * @return 登录响应，包含 JWT token
     */
    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest request) {
        if (request == null) {
            throw new InvalidUserRequestException("请求参数不能为空");
        }

        String username = normalize(request.username());
        String password = normalize(request.password());

        if (username == null) {
            throw new InvalidUserRequestException("用户名不能为空");
        }
        if (password == null) {
            throw new InvalidUserRequestException("密码不能为空");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidLoginException("用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidLoginException("用户名或密码错误");
        }

        return UserLoginResponse.from(user, jwtService.generateToken(user));
    }

    /**
     * 根据 Authorization 请求头解析当前登录用户。
     *
     * @param authorization Authorization 请求头
     * @return 当前登录用户实体
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(String authorization) {
        String token = extractBearerToken(authorization);
        JwtService.JwtUser jwtUser = jwtService.parseToken(token);
        return userRepository.findById(jwtUser.userId())
                .orElseThrow(() -> new com.wuming.blog.user.exception.UnauthorizedException("未登录或登录已失效"));
    }

    /**
     * 根据用户 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在"));
    }

    /**
     * 查询所有用户。
     *
     * @return 用户列表
     */
    /**
     * 管理员按 ID 查询用户详情，不返回密码字段的转换由 Controller 负责。
     *
     * @param id 用户 ID
     * @param authorization Authorization 请求头，必须属于管理员
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public User getByIdForAdmin(Long id, String authorization) {
        requireAdmin(authorization);
        return getById(id);
    }

    @Transactional(readOnly = true)
    public List<User> listUsers(String authorization) {
        requireAdmin(authorization);
        return userRepository.findAll();
    }

    /**
     * 管理员修改指定用户角色。为了避免管理员误把自己锁出后台，禁止自我降级。
     *
     * @param id 用户 ID
     * @param authorization Authorization 请求头，必须属于管理员
     * @param request 角色修改请求
     * @return 修改后的用户
     */
    @Transactional
    public User updateRole(Long id, String authorization, UserRoleUpdateRequest request) {
        User admin = requireAdmin(authorization);
        User user = getById(id);
        UserRole role = parseRole(request);

        if (admin.getId().equals(user.getId()) && role == UserRole.USER) {
            throw new UserPermissionException("管理员不能将自己降级为普通用户");
        }

        user.setRole(role);
        return user;
    }

    /**
     * 管理员删除指定用户。为了保证后台可继续访问，禁止管理员删除自己的账号。
     *
     * @param id 用户 ID
     * @param authorization Authorization 请求头，必须属于管理员
     */
    @Transactional
    public void deleteUser(Long id, String authorization) {
        User admin = requireAdmin(authorization);
        User user = getById(id);

        if (admin.getId().equals(user.getId())) {
            throw new UserPermissionException("管理员不能删除自己的账号");
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidUserRequestException("该用户已关联文章或评论，不能直接删除");
        }
    }

    /**
     * 解析并校验角色请求，避免写入 USER/ADMIN 之外的非法角色。
     */
    private UserRole parseRole(UserRoleUpdateRequest request) {
        if (request == null || normalize(request.role()) == null) {
            throw new InvalidUserRequestException("用户角色不能为空");
        }

        try {
            return UserRole.valueOf(normalize(request.role()).toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserRequestException("用户角色只能是 USER 或 ADMIN");
        }
    }

    /**
     * 从 Authorization 中解析当前用户，并确认其为管理员。
     */
    private User requireAdmin(String authorization) {
        User currentUser = getCurrentUser(authorization);
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new UserPermissionException("只有管理员可以执行该操作");
        }
        return currentUser;
    }

    /**
     * 去除字符串首尾空格，空字符串统一转换为 null。
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 从 Authorization 请求头中提取 Bearer token。
     */
    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new com.wuming.blog.user.exception.UnauthorizedException("未登录或登录已失效");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new com.wuming.blog.user.exception.UnauthorizedException("未登录或登录已失效");
        }
        return token;
    }
}
