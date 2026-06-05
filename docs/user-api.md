# 用户模块 REST API 文档

## 文件说明

| 文件路径 | 作用 |
| --- | --- |
| `src/main/java/com/wuming/blog/user/entity/User.java` | 用户实体类，映射 `users` 表，保存用户名、BCrypt 密码密文和时间字段。 |
| `src/main/java/com/wuming/blog/user/repository/UserRepository.java` | 用户数据访问层，提供用户名查重和查询能力。 |
| `src/main/java/com/wuming/blog/user/service/UserService.java` | 用户业务层，处理注册、密码加密、重复用户名校验和查询。 |
| `src/main/java/com/wuming/blog/user/controller/UserController.java` | 用户 REST 控制器，提供注册、按 id 查询、列表查询接口。 |
| `src/main/java/com/wuming/blog/user/dto/UserRegisterRequest.java` | 用户注册请求体 DTO。 |
| `src/main/java/com/wuming/blog/user/dto/UserResponse.java` | 用户响应 DTO，不包含密码字段。 |
| `src/main/java/com/wuming/blog/user/dto/ApiErrorResponse.java` | 错误响应 DTO。 |
| `src/main/java/com/wuming/blog/user/exception/*.java` | 用户模块异常和统一异常处理。 |
| `src/main/java/com/wuming/blog/user/config/PasswordEncoderConfig.java` | 注册 BCrypt `PasswordEncoder` Bean。 |
| `src/main/java/com/wuming/blog/user/config/SecurityConfig.java` | 开放用户模块 REST API，避免 Spring Security 默认登录拦截。 |
| `docs/user-api.md` | 用户模块 REST API 文档。 |

## 注册用户

- 请求方法：`POST`
- 请求路径：`/api/users/register`
- 说明：注册新用户，密码会使用 BCrypt 加密后保存。

请求示例：

```json
{
  "username": "alice",
  "password": "123456"
}
```

成功响应：`201 Created`

```json
{
  "id": 1,
  "username": "alice",
  "createdAt": "2026-06-05T19:30:00",
  "updatedAt": "2026-06-05T19:30:00"
}
```

用户名重复响应：`400 Bad Request`

```json
{
  "message": "用户名已存在",
  "timestamp": "2026-06-05T19:31:00"
}
```

参数为空响应：`400 Bad Request`

```json
{
  "message": "用户名不能为空",
  "timestamp": "2026-06-05T19:31:00"
}
```

## 根据 id 查询用户

- 请求方法：`GET`
- 请求路径：`/api/users/{id}`
- 说明：查询指定 id 的用户基础信息，不返回密码。

成功响应：`200 OK`

```json
{
  "id": 1,
  "username": "alice",
  "createdAt": "2026-06-05T19:30:00",
  "updatedAt": "2026-06-05T19:30:00"
}
```

用户不存在响应：`404 Not Found`

```json
{
  "message": "用户不存在",
  "timestamp": "2026-06-05T19:32:00"
}
```

## 查询用户列表

- 请求方法：`GET`
- 请求路径：`/api/users`
- 说明：查询所有用户基础信息，不返回密码。

成功响应：`200 OK`

```json
[
  {
    "id": 1,
    "username": "alice",
    "createdAt": "2026-06-05T19:30:00",
    "updatedAt": "2026-06-05T19:30:00"
  }
]
```
