package com.wuming.blog.comment.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 评论分页响应。
 *
 * @param content 当前页评论列表
 * @param page 当前页码，从 0 开始
 * @param size 每页数量
 * @param totalElements 总记录数
 * @param totalPages 总页数
 */
public record CommentPageResponse(
        List<CommentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    /**
     * 将 Spring Data Page 转换为接口分页响应。
     */
    public static CommentPageResponse from(Page<CommentResponse> page) {
        return new CommentPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
