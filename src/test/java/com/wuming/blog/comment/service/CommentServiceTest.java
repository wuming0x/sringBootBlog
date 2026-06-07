package com.wuming.blog.comment.service;

import com.wuming.blog.article.entity.Article;
import com.wuming.blog.article.exception.ArticleNotFoundException;
import com.wuming.blog.article.repository.ArticleRepository;
import com.wuming.blog.comment.dto.CommentPageResponse;
import com.wuming.blog.comment.dto.CommentRequest;
import com.wuming.blog.comment.dto.CommentResponse;
import com.wuming.blog.comment.entity.Comment;
import com.wuming.blog.comment.exception.CommentNotFoundException;
import com.wuming.blog.comment.exception.CommentPermissionException;
import com.wuming.blog.comment.exception.InvalidCommentRequestException;
import com.wuming.blog.comment.repository.CommentRepository;
import com.wuming.blog.user.entity.User;
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
 * 评论业务测试，使用 Mock 隔离数据库访问。
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    /**
     * 评论数据访问 Mock。
     */
    @Mock
    private CommentRepository commentRepository;

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
     * 发布评论时应关联当前登录用户和目标文章。
     */
    @Test
    void createShouldSaveCommentWithCurrentUserAndArticle() {
        User articleAuthor = buildUser(1L, "alice");
        User commentAuthor = buildUser(2L, "bob");
        Article article = buildArticle(1L, articleAuthor);
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(commentAuthor);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
            comment.setUpdatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
            return comment;
        });

        CommentResponse response = commentService.create(
                1L,
                "Bearer token",
                new CommentRequest(" 写得很好 ")
        );

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertEquals("写得很好", response.content());
        assertEquals(1L, response.articleId());
        assertEquals(2L, response.authorId());
        assertEquals(article, commentCaptor.getValue().getArticle());
        assertEquals(commentAuthor, commentCaptor.getValue().getAuthor());
    }

    /**
     * 评论内容为空时应抛出参数异常。
     */
    @Test
    void createShouldRejectBlankContent() {
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(2L, "bob"));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(buildArticle(1L, buildUser(1L, "alice"))));

        InvalidCommentRequestException exception = assertThrows(
                InvalidCommentRequestException.class,
                () -> commentService.create(1L, "Bearer token", new CommentRequest(" "))
        );

        assertEquals("评论内容不能为空", exception.getMessage());
    }

    /**
     * 发布评论时文章不存在应抛出文章不存在异常。
     */
    @Test
    void createShouldRejectMissingArticle() {
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(2L, "bob"));
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> commentService.create(1L, "Bearer token", new CommentRequest("评论"))
        );

        assertEquals("文章不存在", exception.getMessage());
    }

    /**
     * 查询评论列表时文章不存在应抛出文章不存在异常。
     */
    @Test
    void listByArticleShouldRejectMissingArticle() {
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> commentService.listByArticle(1L, 0, 10)
        );

        assertEquals("文章不存在", exception.getMessage());
    }

    /**
     * 分页查询评论应限制分页大小并返回评论响应。
     */
    @Test
    void listByArticleShouldReturnCommentPage() {
        User author = buildUser(2L, "bob");
        Article article = buildArticle(1L, buildUser(1L, "alice"));
        Comment comment = buildComment(1L, article, author);
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentRepository.findByArticleId(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment), PageRequest.of(0, 50), 1));

        CommentPageResponse response = commentService.listByArticle(1L, 0, 100);

        assertEquals(1, response.content().size());
        assertEquals("评论内容", response.content().get(0).content());
        assertEquals(50, response.size());
    }

    /**
     * 评论作者可以删除评论。
     */
    @Test
    void deleteShouldAllowCommentAuthor() {
        User articleAuthor = buildUser(1L, "alice");
        User commentAuthor = buildUser(2L, "bob");
        Comment comment = buildComment(1L, buildArticle(1L, articleAuthor), commentAuthor);
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(commentAuthor);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.delete(1L, "Bearer token");

        verify(commentRepository).delete(comment);
    }

    /**
     * 文章作者可以删除文章下的评论。
     */
    @Test
    void deleteShouldAllowArticleAuthor() {
        User articleAuthor = buildUser(1L, "alice");
        User commentAuthor = buildUser(2L, "bob");
        Comment comment = buildComment(1L, buildArticle(1L, articleAuthor), commentAuthor);
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(articleAuthor);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.delete(1L, "Bearer token");

        verify(commentRepository).delete(comment);
    }

    /**
     * 非评论作者且非文章作者不能删除评论。
     */
    @Test
    void deleteShouldRejectUnauthorizedUser() {
        User articleAuthor = buildUser(1L, "alice");
        User commentAuthor = buildUser(2L, "bob");
        Comment comment = buildComment(1L, buildArticle(1L, articleAuthor), commentAuthor);
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(3L, "charlie"));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        CommentPermissionException exception = assertThrows(
                CommentPermissionException.class,
                () -> commentService.delete(1L, "Bearer token")
        );

        assertEquals("无权删除该评论", exception.getMessage());
    }

    /**
     * 评论不存在时删除应抛出评论不存在异常。
     */
    @Test
    void deleteShouldRejectMissingComment() {
        CommentService commentService = new CommentService(commentRepository, articleRepository, userService);
        when(userService.getCurrentUser("Bearer token")).thenReturn(buildUser(1L, "alice"));
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.delete(1L, "Bearer token")
        );

        assertEquals("评论不存在", exception.getMessage());
    }

    /**
     * 构造用户实体。
     */
    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
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
        article.setCreatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
        article.setUpdatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
        return article;
    }

    /**
     * 构造评论实体。
     */
    private Comment buildComment(Long id, Article article, User author) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent("评论内容");
        comment.setArticle(article);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
        comment.setUpdatedAt(LocalDateTime.of(2026, 6, 7, 10, 0));
        return comment;
    }
}
