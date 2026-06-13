# 博客后端

Spring Boot 后端服务，提供用户、文章、评论等接口能力。

当前测试版本：`v0.1.0-beta.2`

## 功能

- 用户注册、登录
- JWT 登录认证
- 文章列表、详情、发布、编辑、删除
- 评论列表、发布、删除
- MySQL 数据库存储
- 管理员功能
- 多环境配置支持
- 用户管理
  
## 环境要求

- Java 17+
- MySQL

## 文档

接口说明文档在 `docs` 目录下。
## 本地开发

为了保护数据库安全，真实数据库配置不会提交到仓库。

请进入：

```text
src/main/resources/
```

复制：

```text
application-dev.example.properties
```

并重命名为：

```text
application-dev.properties
```

然后根据本地环境修改数据库账号、密码等配置。

本地启动时使用 `dev` 环境。
## 部署
部署参照发布说明

