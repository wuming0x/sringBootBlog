package com.wuming.blog.article.service;

import com.wuming.blog.article.dto.ArticleDetailResponse;
import com.wuming.blog.article.dto.ArticlePageResponse;
import com.wuming.blog.article.dto.ArticleRequest;
import com.wuming.blog.article.entity.Article;
import com.wuming.blog.article.exception.ArticleNotFoundException;
import com.wuming.blog.article.exception.ArticlePermissionException;
import com.wuming.blog.article.exception.InvalidArticleRequestException;
import com.wuming.blog.article.repository.ArticleRepository;
import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.entity.UserRole;
import com.wuming.blog.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 文章业务测试，使用 Mock 隔离数据库访问。
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    /**
     * 文章数据访问 Mock。
     */
    @Mock
    private ArticleRepository articleRepository;

    /**
     * 用户服务 Mock，用于模拟当前登录用户。
     */
    @Mock
    private UserService userService;

    /**
     * 创建文章时应关联当前登录用户，并在摘要为空时自动生成摘要。
     */
    @Test
    void createShouldSaveArticleWithCurrentUser() {
        User author = buildUser(1L, "alice");
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(author);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(1L);
            article.setCreatedAt(LocalDateTime.of(2026, 6, 6, 10, 0));
            article.setUpdatedAt(LocalDateTime.of(2026, 6, 6, 10, 0));
            return article;
        });

        ArticleDetailResponse response = articleService.create(
                "Bearer token",
                new ArticleRequest("标题", "", "这是一段文章正文")
        );

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());
        assertEquals("标题", response.title());
        assertEquals("这是一段文章正文", response.summary());
        assertEquals(1L, response.authorId());
        assertEquals(author, articleCaptor.getValue().getAuthor());
    }

    /**
     * 标题为空时应抛出参数异常。
     */
    @Test
    void createShouldRejectBlankTitle() {
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(1L, "alice"));

        InvalidArticleRequestException exception = assertThrows(
                InvalidArticleRequestException.class,
                () -> articleService.create("Bearer token", new ArticleRequest(" ", "摘要", "正文"))
        );

        assertEquals("标题不能为空", exception.getMessage());
    }

    /**
     * 正文为空时应抛出参数异常。
     */
    @Test
    void createShouldRejectBlankContent() {
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(1L, "alice"));

        InvalidArticleRequestException exception = assertThrows(
                InvalidArticleRequestException.class,
                () -> articleService.create("Bearer token", new ArticleRequest("标题", "摘要", " "))
        );

        assertEquals("正文不能为空", exception.getMessage());
    }

    /**
     * 分页列表应返回摘要，不返回正文。
     */
    @Test
    void listShouldReturnSummaryPage() {
        ArticleService articleService = new ArticleService(articleRepository, userService);
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        when(articleRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(article), PageRequest.of(0, 50), 1));

        ArticlePageResponse response = articleService.list(0, 100);

        assertEquals(1, response.content().size());
        assertEquals("摘要", response.content().get(0).summary());
        assertEquals(50, response.size());
    }

    /**
     * 文章详情应返回完整正文。
     */
    @Test
    void getByIdShouldReturnDetailWithContent() {
        ArticleService articleService = new ArticleService(articleRepository, userService);
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleDetailResponse response = articleService.getById(1L);

        assertEquals("正文", response.content());
    }

    /**
     * 作者可以编辑文章。
     */
    @Test
    void updateShouldAllowAuthor() {
        User author = buildUser(1L, "alice");
        Article article = buildArticle(1L, author);
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleDetailResponse response = articleService.update(
                1L,
                "Bearer token",
                new ArticleRequest("新标题", "新摘要", "新正文")
        );

        assertEquals("新标题", response.title());
        assertEquals("新正文", response.content());
    }

    /**
     * 非作者不能编辑文章。
     */
    @Test
    void updateShouldRejectNonAuthor() {
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(2L, "bob"));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticlePermissionException exception = assertThrows(
                ArticlePermissionException.class,
                () -> articleService.update(1L, "Bearer token", new ArticleRequest("新标题", "新摘要", "新正文"))
        );

        assertEquals("无权操作该文章", exception.getMessage());
    }

    /**
     * 管理员可以编辑他人的文章。
     */
    @Test
    void updateShouldAllowAdmin() {
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildAdminUser(2L, "admin"));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleDetailResponse response = articleService.update(
                1L,
                "Bearer token",
                new ArticleRequest("管理员修改", "摘要", "正文")
        );

        assertEquals("管理员修改", response.title());
    }

    /**
     * 作者可以删除文章。
     */
    @Test
    void deleteShouldAllowAuthor() {
        User author = buildUser(1L, "alice");
        Article article = buildArticle(1L, author);
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.delete(1L, "Bearer token");

        verify(articleRepository).delete(article);
    }

    /**
     * 管理员可以删除他人的文章。
     */
    @Test
    void deleteShouldAllowAdmin() {
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildAdminUser(2L, "admin"));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.delete(1L, "Bearer token");

        verify(articleRepository).delete(article);
    }

    /**
     * 文章不存在时应抛出异常。
     */
    @Test
    void getByIdShouldRejectMissingArticle() {
        ArticleService articleService = new ArticleService(articleRepository, userService);
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.getById(1L)
        );

        assertEquals("文章不存在", exception.getMessage());
    }

    /**
     * 构造用户实体。
     */
    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(UserRole.USER);
        return user;
    }

    /**
     * 构造管理员用户实体。
     */
    private User buildAdminUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(UserRole.ADMIN);
        return user;
    }

    /**
     * 构造文章实体。
     */
    private Article buildArticle(Long id, User author) {
        Article article = new Article();
        article.setId(id);
        article.setTitle("标题");
        article.setSummary("摘要");
        article.setContent("正文");
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.of(2026, 6, 6, 10, 0));
        article.setUpdatedAt(LocalDateTime.of(2026, 6, 6, 10, 0));
        return article;
    }
}
