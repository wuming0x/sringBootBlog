package com.wuming.blog.comment.controller;

import com.wuming.blog.comment.dto.CommentPageResponse;
import com.wuming.blog.comment.dto.CommentRequest;
import com.wuming.blog.comment.dto.CommentResponse;
import com.wuming.blog.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论 REST 控制器，提供评论发布、分页查询和删除接口。
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    /**
     * 评论业务服务。
     */
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 分页查询指定文章下的评论。
     */
    @GetMapping("/articles/{articleId}/comments")
    public CommentPageResponse listByArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.listByArticle(articleId, page, size);
    }

    /**
     * 发布评论，必须登录。
     */
    @PostMapping("/articles/{articleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable Long articleId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CommentRequest request
    ) {
        return commentService.create(articleId, authorization, request);
    }

    /**
     * 删除评论，必须登录，评论作者或文章作者可以操作。
     */
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        commentService.delete(commentId, authorization);
    }
}
