# 文章模块 REST API 文档

## 文件说明

| 文件路径 | 作用 |
| --- | --- |
| `src/main/java/com/wuming/blog/article/entity/Article.java` | 文章实体类，映射 `articles` 表，保存标题、摘要、正文、作者和时间字段。 |
| `src/main/java/com/wuming/blog/article/repository/ArticleRepository.java` | 文章数据访问层，提供基础 CRUD 和分页查询能力。 |
| `src/main/java/com/wuming/blog/article/service/ArticleService.java` | 文章业务层，处理创建、分页列表、详情、编辑、删除和作者权限校验。 |
| `src/main/java/com/wuming/blog/article/controller/ArticleController.java` | 文章 REST 控制器，提供文章 CRUD 接口。 |
| `src/main/java/com/wuming/blog/article/dto/*.java` | 文章请求、详情响应、列表项响应和分页响应 DTO。 |
| `src/main/java/com/wuming/blog/article/exception/*.java` | 文章模块异常和统一异常处理。 |
| `src/main/java/com/wuming/blog/user/service/JwtService.java` | 轻量 JWT 服务，登录成功后签发 token，文章写操作通过 token 识别作者。 |
| `docs/article-api.md` | 文章模块 REST API 文档。 |

## 鉴权说明

文章列表和详情公开访问。创建、编辑、删除文章需要登录，并在请求头中传入登录接口返回的 token。

```http
Authorization: Bearer <token>
```

未登录或 token 无效返回 `401 Unauthorized`。编辑和删除文章允许文章作者或管理员操作；管理员角色为 `ADMIN`。

## 创建文章

- 请求方法：`POST`
- 请求路径：`/api/articles`
- 是否需要登录：是

请求示例：

```json
{
  "title": "第一篇文章",
  "summary": "可选摘要",
  "content": "文章正文"
}
```

成功响应：`201 Created`

```json
{
  "id": 1,
  "title": "第一篇文章",
  "summary": "可选摘要",
  "content": "文章正文",
  "authorId": 1,
  "authorUsername": "alice",
  "createdAt": "2026-06-06T10:00:00",
  "updatedAt": "2026-06-06T10:00:00"
}
```

参数错误响应：`400 Bad Request`

```json
{
  "message": "标题不能为空",
  "timestamp": "2026-06-06T10:01:00"
}
```

## 分页查询文章列表

- 请求方法：`GET`
- 请求路径：`/api/articles?page=0&size=10`
- 是否需要登录：否
- 说明：`page` 从 0 开始，默认 `0`；`size` 默认 `10`，最大 `50`。列表按 `createdAt DESC` 排序，不返回完整正文。

成功响应：`200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "title": "第一篇文章",
      "summary": "可选摘要",
      "authorId": 1,
      "authorUsername": "alice",
      "createdAt": "2026-06-06T10:00:00",
      "updatedAt": "2026-06-06T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

## 查询文章详情

- 请求方法：`GET`
- 请求路径：`/api/articles/{id}`
- 是否需要登录：否

成功响应：`200 OK`

```json
{
  "id": 1,
  "title": "第一篇文章",
  "summary": "可选摘要",
  "content": "文章正文",
  "authorId": 1,
  "authorUsername": "alice",
  "createdAt": "2026-06-06T10:00:00",
  "updatedAt": "2026-06-06T10:00:00"
}
```

文章不存在响应：`404 Not Found`

```json
{
  "message": "文章不存在",
  "timestamp": "2026-06-06T10:02:00"
}
```

## 编辑文章

- 请求方法：`PUT`
- 请求路径：`/api/articles/{id}`
- 是否需要登录：是，文章作者或管理员可编辑

请求示例：

```json
{
  "title": "更新后的标题",
  "summary": "",
  "content": "更新后的文章正文"
}
```

成功响应：`200 OK`

```json
{
  "id": 1,
  "title": "更新后的标题",
  "summary": "更新后的摘要",
  "content": "更新后的文章正文",
  "authorId": 1,
  "authorUsername": "alice",
  "createdAt": "2026-06-06T10:00:00",
  "updatedAt": "2026-06-06T10:10:00"
}
```

无权限操作响应：`403 Forbidden`

```json
{
  "message": "无权操作该文章",
  "timestamp": "2026-06-06T10:10:00"
}
```

## 删除文章

- 请求方法：`DELETE`
- 请求路径：`/api/articles/{id}`
- 是否需要登录：是，文章作者或管理员可删除
- 说明：删除方式为物理删除。

成功响应：`204 No Content`
