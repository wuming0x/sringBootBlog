package com.wuming.blog.article.controller;

import com.wuming.blog.article.dto.ArticleDetailResponse;
import com.wuming.blog.article.dto.ArticlePageResponse;
import com.wuming.blog.article.dto.ArticleRequest;
import com.wuming.blog.article.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章 REST 控制器，提供文章创建、分页查询、详情查询、编辑和删除接口。
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    /**
     * 文章业务服务。
     */
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 创建文章，需要登录。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDetailResponse create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ArticleRequest request
    ) {
        return articleService.create(authorization, request);
    }

    /**
     * 分页查询文章摘要列表。
     */
    @GetMapping
    public ArticlePageResponse list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleService.list(page, size);
    }

    /**
     * 查询文章详情。
     */
    @GetMapping("/{id}")
    public ArticleDetailResponse getById(@PathVariable Long id) {
        return articleService.getById(id);
    }

    /**
     * 编辑文章，需要登录且仅作者可操作。
     */
    @PutMapping("/{id}")
    public ArticleDetailResponse update(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ArticleRequest request
    ) {
        return articleService.update(id, authorization, request);
    }

    /**
     * 删除文章，需要登录且仅作者可操作。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        articleService.delete(id, authorization);
    }
}
