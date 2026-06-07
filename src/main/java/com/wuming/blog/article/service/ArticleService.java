package com.wuming.blog.article.service;

import com.wuming.blog.article.dto.ArticleDetailResponse;
import com.wuming.blog.article.dto.ArticleListItemResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章业务服务，负责文章创建、分页查询、详情查询、编辑和删除。
 */
@Service
public class ArticleService {

    /**
     * 默认分页大小。
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小，防止一次查询过多数据。
     */
    private static final int MAX_PAGE_SIZE = 50;

    /**
     * 自动摘要最大长度。
     */
    private static final int SUMMARY_LENGTH = 120;

    /**
     * 文章数据访问对象。
     */
    private final ArticleRepository articleRepository;

    /**
     * 用户服务，用于从 Bearer token 解析当前登录用户。
     */
    private final UserService userService;

    public ArticleService(ArticleRepository articleRepository, UserService userService) {
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    /**
     * 创建文章，并将作者设置为当前登录用户。
     */
    @Transactional
    public ArticleDetailResponse create(String authorization, ArticleRequest request) {
        User author = userService.getCurrentUser(authorization);
        Article article = new Article();
        applyRequest(article, request);
        article.setAuthor(author);
        return ArticleDetailResponse.from(articleRepository.save(article));
    }

    /**
     * 分页查询文章摘要列表。
     */
    @Transactional(readOnly = true)
    public ArticlePageResponse list(int page, int size) {
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<ArticleListItemResponse> articles = articleRepository.findAll(pageable)
                .map(ArticleListItemResponse::from);
        return ArticlePageResponse.from(articles);
    }

    /**
     * 查询文章详情。
     */
    @Transactional(readOnly = true)
    public ArticleDetailResponse getById(Long id) {
        return ArticleDetailResponse.from(findArticle(id));
    }

    /**
     * 编辑文章，文章作者或管理员可以操作。
     */
    @Transactional
    public ArticleDetailResponse update(Long id, String authorization, ArticleRequest request) {
        User currentUser = userService.getCurrentUser(authorization);
        Article article = findArticle(id);
        checkAuthor(article, currentUser);
        applyRequest(article, request);
        return ArticleDetailResponse.from(article);
    }

    /**
     * 物理删除文章，文章作者或管理员可以操作。
     */
    @Transactional
    public void delete(Long id, String authorization) {
        User currentUser = userService.getCurrentUser(authorization);
        Article article = findArticle(id);
        checkAuthor(article, currentUser);
        articleRepository.delete(article);
    }

    /**
     * 根据 ID 查询文章实体。
     */
    private Article findArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("文章不存在"));
    }

    /**
     * 将请求参数应用到文章实体。
     */
    private void applyRequest(Article article, ArticleRequest request) {
        if (request == null) {
            throw new InvalidArticleRequestException("请求参数不能为空");
        }

        String title = normalize(request.title());
        String content = normalize(request.content());
        String summary = normalize(request.summary());

        if (title == null) {
            throw new InvalidArticleRequestException("标题不能为空");
        }
        if (content == null) {
            throw new InvalidArticleRequestException("正文不能为空");
        }

        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary == null ? buildSummary(content) : summary);
    }

    /**
     * 校验当前用户是否为文章作者或管理员。
     */
    private void checkAuthor(Article article, User currentUser) {
        boolean isAuthor = article.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new ArticlePermissionException("无权操作该文章");
        }
    }

    /**
     * 生成默认摘要，最长 120 个字符。
     */
    private String buildSummary(String content) {
        return content.length() <= SUMMARY_LENGTH ? content : content.substring(0, SUMMARY_LENGTH);
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
