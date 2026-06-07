package com.wuming.blog.comment.controller;

import com.wuming.blog.article.exception.ArticleExceptionHandler;
import com.wuming.blog.comment.dto.CommentPageResponse;
import com.wuming.blog.comment.dto.CommentResponse;
import com.wuming.blog.comment.exception.CommentExceptionHandler;
import com.wuming.blog.comment.exception.CommentPermissionException;
import com.wuming.blog.comment.service.CommentService;
import com.wuming.blog.user.exception.UnauthorizedException;
import com.wuming.blog.user.exception.UserExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 评论接口测试，使用 MockMvc 验证 REST 响应。
 */
class CommentControllerTest {

    /**
     * 查询文章评论列表应返回分页字段和评论项。
     */
    @Test
    void listByArticleShouldReturnCommentPage() throws Exception {
        CommentService commentService = mock(CommentService.class);
        MockMvc mockMvc = buildMockMvc(commentService);
        when(commentService.listByArticle(1L, 0, 10)).thenReturn(new CommentPageResponse(
                List.of(buildResponse()),
                0,
                10,
                1,
                1
        ));

        mockMvc.perform(get("/api/articles/1/comments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("评论内容"))
                .andExpect(jsonPath("$.content[0].articleId").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    /**
     * 发布评论需要 Bearer token，成功时返回 201。
     */
    @Test
    void createShouldReturnCreatedComment() throws Exception {
        CommentService commentService = mock(CommentService.class);
        MockMvc mockMvc = buildMockMvc(commentService);
        when(commentService.create(eq(1L), eq("Bearer token"), any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/articles/1/comments")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "评论内容"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("评论内容"));
    }

    /**
     * 发布评论缺少 token 时应返回 401。
     */
    @Test
    void createShouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        CommentService commentService = mock(CommentService.class);
        MockMvc mockMvc = buildMockMvc(commentService);
        when(commentService.create(eq(1L), eq(null), any()))
                .thenThrow(new UnauthorizedException("未登录或登录已失效"));

        mockMvc.perform(post("/api/articles/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "评论内容"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    /**
     * 删除评论成功时返回 204。
     */
    @Test
    void deleteShouldReturnNoContent() throws Exception {
        CommentService commentService = mock(CommentService.class);
        MockMvc mockMvc = buildMockMvc(commentService);

        mockMvc.perform(delete("/api/comments/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    /**
     * 无权限删除评论时返回 403。
     */
    @Test
    void deleteShouldReturnForbiddenWhenUserHasNoPermission() throws Exception {
        CommentService commentService = mock(CommentService.class);
        MockMvc mockMvc = buildMockMvc(commentService);
        doThrow(new CommentPermissionException("无权删除该评论"))
                .when(commentService).delete(1L, "Bearer token");

        mockMvc.perform(delete("/api/comments/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("无权删除该评论"));
    }

    /**
     * 构建带异常处理器的 MockMvc。
     */
    private MockMvc buildMockMvc(CommentService commentService) {
        return MockMvcBuilders
                .standaloneSetup(new CommentController(commentService))
                .setControllerAdvice(
                        new CommentExceptionHandler(),
                        new ArticleExceptionHandler(),
                        new UserExceptionHandler()
                )
                .build();
    }

    /**
     * 构造评论响应。
     */
    private CommentResponse buildResponse() {
        return new CommentResponse(
                1L,
                "评论内容",
                1L,
                2L,
                "bob",
                LocalDateTime.of(2026, 6, 7, 10, 0),
                LocalDateTime.of(2026, 6, 7, 10, 0)
        );
    }
}
