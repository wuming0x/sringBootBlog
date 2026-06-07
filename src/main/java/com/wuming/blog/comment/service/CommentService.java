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
import com.wuming.blog.user.entity.UserRole;
import com.wuming.blog.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论业务服务，负责发布、分页查询和删除评论。
 */
@Service
public class CommentService {

    /**
     * 默认分页大小。
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小，防止一次查询过多数据。
     */
    private static final int MAX_PAGE_SIZE = 50;

    /**
     * 评论数据访问对象。
     */
    private final CommentRepository commentRepository;

    /**
     * 文章数据访问对象，用于校验文章是否存在。
     */
    private final ArticleRepository articleRepository;

    /**
     * 用户服务，用于从 Bearer token 解析当前登录用户。
     */
    private final UserService userService;

    public CommentService(
            CommentRepository commentRepository,
            ArticleRepository articleRepository,
            UserService userService
    ) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    /**
     * 发布评论，必须登录，并将评论关联到指定文章。
     */
    @Transactional
    public CommentResponse create(Long articleId, String authorization, CommentRequest request) {
        User author = userService.getCurrentUser(authorization);
        Article article = findArticle(articleId);
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(author);
        comment.setContent(extractContent(request));
        return CommentResponse.from(commentRepository.save(comment));
    }

    /**
     * 分页查询指定文章下的评论。
     */
    @Transactional(readOnly = true)
    public CommentPageResponse listByArticle(Long articleId, int page, int size) {
        findArticle(articleId);
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        Page<CommentResponse> comments = commentRepository.findByArticleId(articleId, pageable)
                .map(CommentResponse::from);
        return CommentPageResponse.from(comments);
    }

    /**
     * 删除评论，评论作者、文章作者或管理员可以操作。
     */
    @Transactional
    public void delete(Long commentId, String authorization) {
        User currentUser = userService.getCurrentUser(authorization);
        Comment comment = findComment(commentId);
        checkDeletePermission(comment, currentUser);
        commentRepository.delete(comment);
    }

    /**
     * 根据 ID 查询文章实体。
     */
    private Article findArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("文章不存在"));
    }

    /**
     * 根据 ID 查询评论实体。
     */
    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("评论不存在"));
    }

    /**
     * 校验当前用户是否可以删除评论。
     */
    private void checkDeletePermission(Comment comment, User currentUser) {
        Long currentUserId = currentUser.getId();
        boolean isCommentAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isArticleAuthor = comment.getArticle().getAuthor().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isCommentAuthor && !isArticleAuthor && !isAdmin) {
            throw new CommentPermissionException("无权删除该评论");
        }
    }

    /**
     * 提取并校验评论正文。
     */
    private String extractContent(CommentRequest request) {
        if (request == null) {
            throw new InvalidCommentRequestException("请求参数不能为空");
        }
        String content = normalize(request.content());
        if (content == null) {
            throw new InvalidCommentRequestException("评论内容不能为空");
        }
        return content;
    }

    /**
     * 规范分页页码，最小为 0。
     */
    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    /**
     * 规范分页大小，默认 10，最大 50。
     */
    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /**
     * 去除字符串首尾空格，空字符串统一转换为 null。
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
