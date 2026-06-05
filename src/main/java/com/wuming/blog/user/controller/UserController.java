package com.wuming.blog.user.controller;

import com.wuming.blog.user.dto.UserRegisterRequest;
import com.wuming.blog.user.dto.UserResponse;
import com.wuming.blog.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 REST 控制器，提供注册和查询接口。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 用户业务服务。
     */
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册用户，注册成功后返回用户基础信息。
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return UserResponse.from(userService.register(request));
    }

    /**
     * 根据用户 ID 查询用户基础信息。
     */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserResponse.from(userService.getById(id));
    }

    /**
     * 查询用户列表。
     */
    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listUsers()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
