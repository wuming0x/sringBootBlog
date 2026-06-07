# 评论模块 REST API 文档

## 文件说明

| 文件路径 | 作用 |
| --- | --- |
| `src/main/java/com/wuming/blog/comment/entity/Comment.java` | 评论实体类，映射 `comments` 表，保存评论正文、所属文章、作者和时间字段。 |
| `src/main/java/com/wuming/blog/comment/repository/CommentRepository.java` | 评论数据访问层，提供基础 CRUD 和按文章 ID 分页查询评论的能力。 |
| `src/main/java/com/wuming/blog/comment/service/CommentService.java` | 评论业务层，处理发布评论、分页查询、删除评论、登录校验、文章存在校验和删除权限校验。 |
| `src/main/java/com/wuming/blog/comment/controller/CommentController.java` | 评论 REST 控制器，提供文章评论列表、发布评论和删除评论接口。 |
| `src/main/java/com/wuming/blog/comment/dto/CommentRequest.java` | 评论发布请求体 DTO。 |
| `src/main/java/com/wuming/blog/comment/dto/CommentResponse.java` | 评论响应 DTO，包含评论正文、文章 ID、作者信息和时间字段。 |
| `src/main/java/com/wuming/blog/comment/dto/CommentPageResponse.java` | 评论分页响应 DTO。 |
| `src/main/java/com/wuming/blog/comment/exception/*.java` | 评论模块异常和统一异常处理。 |
| `docs/comment-api.md` | 评论模块 REST API 文档。 |

## 鉴权说明

评论列表公开访问。发布评论和删除评论需要登录，并在请求头中传入登录接口返回的 token。

```http
Authorization: Bearer <token>
```

未登录或 token 无效返回 `401 Unauthorized`。删除评论允许评论作者、文章作者或管理员操作；管理员角色为 `ADMIN`。

## 查询文章评论列表

- 请求方法：`GET`
- 请求路径：`/api/articles/{articleId}/comments?page=0&size=10`
- 是否需要登录：否
- 说明：`page` 从 `0` 开始，默认 `0`；`size` 默认 `10`，最大 `50`。评论按 `createdAt ASC` 排序。

成功响应：`200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "content": "写得很好",
      "articleId": 1,
      "authorId": 2,
      "authorUsername": "bob",
      "createdAt": "2026-06-07T10:00:00",
      "updatedAt": "2026-06-07T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

文章不存在响应：`404 Not Found`

```json
{
  "message": "文章不存在",
  "timestamp": "2026-06-07T10:01:00"
}
```

## 发布评论

- 请求方法：`POST`
- 请求路径：`/api/articles/{articleId}/comments`
- 是否需要登录：是

请求示例：

```json
{
  "content": "写得很好"
}
```

成功响应：`201 Created`

```json
{
  "id": 1,
  "content": "写得很好",
  "articleId": 1,
  "authorId": 2,
  "authorUsername": "bob",
  "createdAt": "2026-06-07T10:00:00",
  "updatedAt": "2026-06-07T10:00:00"
}
```

参数错误响应：`400 Bad Request`

```json
{
  "message": "评论内容不能为空",
  "timestamp": "2026-06-07T10:02:00"
}
```

未登录响应：`401 Unauthorized`

```json
{
  "message": "未登录或登录已失效",
  "timestamp": "2026-06-07T10:03:00"
}
```

## 删除评论

- 请求方法：`DELETE`
- 请求路径：`/api/comments/{commentId}`
- 是否需要登录：是
- 说明：评论作者、文章作者或管理员可以删除评论，删除方式为物理删除。

成功响应：`204 No Content`

评论不存在响应：`404 Not Found`

```json
{
  "message": "评论不存在",
  "timestamp": "2026-06-07T10:04:00"
}
```

无权限响应：`403 Forbidden`

```json
{
  "message": "无权删除该评论",
  "timestamp": "2026-06-07T10:05:00"
}
```
