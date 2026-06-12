# 用户模块 REST API 文档

## 文件说明

| 文件路径 | 作用 |
| --- | --- |
| `src/main/java/com/wuming/blog/user/entity/User.java` | 用户实体类，映射 `users` 表，保存用户名、BCrypt 密码密文、角色和时间字段。 |
| `src/main/java/com/wuming/blog/user/entity/UserRole.java` | 用户角色枚举，包含普通用户 `USER` 和管理员 `ADMIN`。 |
| `src/main/java/com/wuming/blog/user/repository/UserRepository.java` | 用户数据访问层，提供用户名查重和查询能力。 |
| `src/main/java/com/wuming/blog/user/service/UserService.java` | 用户业务层，处理注册、登录、JWT 当前用户解析、管理员用户管理等业务。 |
| `src/main/java/com/wuming/blog/user/controller/UserController.java` | 用户 REST 控制器，提供注册、登录和管理员用户管理接口。 |
| `src/main/java/com/wuming/blog/user/dto/UserRegisterRequest.java` | 用户注册请求体 DTO。 |
| `src/main/java/com/wuming/blog/user/dto/UserLoginRequest.java` | 用户登录请求体 DTO。 |
| `src/main/java/com/wuming/blog/user/dto/UserLoginResponse.java` | 用户登录响应 DTO，包含 `id`、`username`、`role`、JWT `token`。 |
| `src/main/java/com/wuming/blog/user/dto/UserRoleUpdateRequest.java` | 管理员修改用户角色请求体 DTO。 |
| `src/main/java/com/wuming/blog/user/service/JwtService.java` | 轻量 JWT 服务，负责签发和解析登录令牌。 |
| `src/main/java/com/wuming/blog/user/dto/UserResponse.java` | 用户响应 DTO，不包含密码字段。 |
| `src/main/java/com/wuming/blog/user/dto/ApiErrorResponse.java` | 错误响应 DTO。 |
| `src/main/java/com/wuming/blog/user/exception/*.java` | 用户模块异常和统一异常处理。 |
| `src/main/java/com/wuming/blog/user/config/PasswordEncoderConfig.java` | 注册 BCrypt `PasswordEncoder` Bean。 |
| `src/main/java/com/wuming/blog/user/config/SecurityConfig.java` | 关闭 Spring Security 默认登录拦截，具体接口权限由业务层根据 JWT 校验。 |
| `docs/user-api.md` | 用户模块 REST API 文档。 |

## 角色说明

用户角色包含：

- `USER`：普通用户，只能管理自己创建的内容。
- `ADMIN`：管理员，可以编辑、删除所有人的文章，并删除所有人的评论，也可以进入隐藏用户管理界面管理用户。

注册接口不会创建管理员，新用户默认角色为 `USER`。管理员账号通过数据库手动设置：

```sql
UPDATE users SET role = 'ADMIN' WHERE username = '你的管理员用户名';
```

如果已有数据库没有 `role` 字段，且 Hibernate 没有自动补齐字段，可手动执行：

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
```

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
  "role": "USER",
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

## 用户登录

- 请求方法：`POST`
- 请求路径：`/api/users/login`
- 说明：使用用户名和密码登录。登录成功后返回 JWT，后续需要登录的接口可通过 `Authorization: Bearer <token>` 传递身份。

请求示例：

```json
{
  "username": "alice",
  "password": "123456"
}
```

成功响应：`200 OK`

```json
{
  "id": 1,
  "username": "alice",
  "role": "USER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

登录失败响应：`400 Bad Request`

```json
{
  "message": "用户名或密码错误",
  "timestamp": "2026-06-05T19:33:00"
}
```

参数为空响应：`400 Bad Request`

```json
{
  "message": "密码不能为空",
  "timestamp": "2026-06-05T19:33:00"
}
```

## 管理员用户管理接口

以下接口仅管理员可用，均需要在请求头中携带 `Authorization: Bearer <token>`。如果未登录返回 `401 Unauthorized`，如果当前用户不是管理员返回 `403 Forbidden`。

### 根据 ID 查询用户

- 请求方法：`GET`
- 请求路径：`/api/users/{id}`
- 权限要求：管理员
- 说明：查询指定用户基础信息，不返回密码字段。

请求头示例：

```http
Authorization: Bearer <token>
```

成功响应：`200 OK`

```json
{
  "id": 1,
  "username": "alice",
  "role": "USER",
  "createdAt": "2026-06-05T19:30:00",
  "updatedAt": "2026-06-05T19:30:00"
}
```

常见错误响应：

- `401 Unauthorized`：缺少或携带无效 token。
- `403 Forbidden`：当前登录用户不是管理员。
- `404 Not Found`：用户不存在。

### 查询用户列表

- 请求方法：`GET`
- 请求路径：`/api/users`
- 权限要求：管理员
- 说明：查询所有用户基础信息，不返回密码字段。

请求头示例：

```http
Authorization: Bearer <token>
```

成功响应：`200 OK`

```json
[
  {
    "id": 1,
    "username": "alice",
    "role": "USER",
    "createdAt": "2026-06-05T19:30:00",
    "updatedAt": "2026-06-05T19:30:00"
  }
]
```

常见错误响应：

- `401 Unauthorized`：缺少或携带无效 token。
- `403 Forbidden`：当前登录用户不是管理员。

### 修改用户角色

- 请求方法：`PUT`
- 请求路径：`/api/users/{id}/role`
- 权限要求：管理员
- 说明：管理员可以将其他用户设置为 `USER` 或 `ADMIN`。为了避免管理员失去后台访问权限，不允许管理员将自己的角色改为 `USER`。

请求头示例：

```http
Authorization: Bearer <token>
```

请求体示例：

```json
{
  "role": "ADMIN"
}
```

成功响应：`200 OK`

```json
{
  "id": 2,
  "username": "bob",
  "role": "ADMIN",
  "createdAt": "2026-06-05T19:30:00",
  "updatedAt": "2026-06-12T09:15:00"
}
```

常见错误响应：

- `400 Bad Request`：角色为空或不是 `USER`/`ADMIN`。
- `401 Unauthorized`：缺少或携带无效 token。
- `403 Forbidden`：非管理员访问，或管理员尝试将自己降级为普通用户。
- `404 Not Found`：用户不存在。

### 删除用户

- 请求方法：`DELETE`
- 请求路径：`/api/users/{id}`
- 权限要求：管理员
- 说明：管理员可以删除其他用户。为了保证后台仍有可用账号，不允许管理员删除自己的账号；如果用户已关联文章或评论，则不能直接删除。

请求头示例：

```http
Authorization: Bearer <token>
```

成功响应：`204 No Content`

常见错误响应：

- `400 Bad Request`：用户已关联文章或评论，不能直接删除。
- `401 Unauthorized`：缺少或携带无效 token。
- `403 Forbidden`：非管理员访问，或管理员尝试删除自己的账号。
- `404 Not Found`：用户不存在。
