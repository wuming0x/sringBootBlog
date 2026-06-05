package com.wuming.blog.user.service;

import com.wuming.blog.user.dto.UserRegisterRequest;
import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.exception.DuplicateUsernameException;
import com.wuming.blog.user.exception.InvalidUserRequestException;
import com.wuming.blog.user.exception.UserNotFoundException;
import com.wuming.blog.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


  //用户业务服务，负责注册、查询和密码加密等业务逻辑。

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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        return userRepository.save(user);
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
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
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
}
