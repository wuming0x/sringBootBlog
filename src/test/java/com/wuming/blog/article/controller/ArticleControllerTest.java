package com.wuming.blog.article.controller;

import com.wuming.blog.article.dto.ArticleDetailResponse;
import com.wuming.blog.article.dto.ArticleListItemResponse;
import com.wuming.blog.article.dto.ArticlePageResponse;
import com.wuming.blog.article.exception.ArticleExceptionHandler;
import com.wuming.blog.article.service.ArticleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文章接口测试，使用 MockMvc 验证 REST 响应。
 */
class ArticleControllerTest {

    /**
     * 创建文章需要 Bearer token，成功时返回 201。
     */
    @Test
    void createShouldReturnCreatedArticle() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        when(articleService.create(eq("Bearer token"), any())).thenReturn(buildDetailResponse());

        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "标题",
                                  "summary": "摘要",
                                  "content": "正文"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("正文"));
    }

    /**
     * 写操作缺少 token 时应返回 401。
     */
    @Test
    void createShouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        when(articleService.create(eq(null), any())).thenThrow(new UnauthorizedException("未登录或登录已失效"));

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "标题",
                                  "content": "正文"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    /**
     * 分页查询文章列表应返回分页字段和摘要项。
     */
    @Test
    void listShouldReturnArticlePage() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        when(articleService.list(0, 10)).thenReturn(new ArticlePageResponse(
                List.of(buildListItemResponse()),
                0,
                10,
                1,
                1
        ));

        mockMvc.perform(get("/api/articles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].summary").value("摘要"))
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    /**
     * 查询文章详情应返回完整正文。
     */
    @Test
    void getByIdShouldReturnArticleDetail() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        when(articleService.getById(1L)).thenReturn(buildDetailResponse());

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("正文"));
    }

    /**
     * 编辑文章需要 Bearer token。
     */
    @Test
    void updateShouldReturnUpdatedArticle() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        when(articleService.update(eq(1L), eq("Bearer token"), any())).thenReturn(buildDetailResponse());

        mockMvc.perform(put("/api/articles/1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "新标题",
                                  "summary": "新摘要",
                                  "content": "新正文"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    /**
     * 删除文章成功时返回 204。
     */
    @Test
    void deleteShouldReturnNoContent() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);

        mockMvc.perform(delete("/api/articles/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    /**
     * 删除文章 token 无效时返回 401。
     */
    @Test
    void deleteShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        MockMvc mockMvc = buildMockMvc(articleService);
        doThrow(new UnauthorizedException("未登录或登录已失效"))
                .when(articleService).delete(1L, "Bearer bad-token");

        mockMvc.perform(delete("/api/articles/1")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录或登录已失效"));
    }

    /**
     * 构建带异常处理器的 MockMvc。
     */
    private MockMvc buildMockMvc(ArticleService articleService) {
        return MockMvcBuilders
                .standaloneSetup(new ArticleController(articleService))
                .setControllerAdvice(new ArticleExceptionHandler(), new UserExceptionHandler())
                .build();
    }

    /**
     * 构造文章详情响应。
     */
    private ArticleDetailResponse buildDetailResponse() {
        return new ArticleDetailResponse(
                1L,
                "标题",
                "摘要",
                "正文",
                1L,
                "alice",
                LocalDateTime.of(2026, 6, 6, 10, 0),
                LocalDateTime.of(2026, 6, 6, 10, 0)
        );
    }

    /**
     * 构造文章列表项响应。
     */
    private ArticleListItemResponse buildListItemResponse() {
        return new ArticleListItemResponse(
                1L,
                "标题",
                "摘要",
                1L,
                "alice",
                LocalDateTime.of(2026, 6, 6, 10, 0),
                LocalDateTime.of(2026, 6, 6, 10, 0)
        );
    }
}
