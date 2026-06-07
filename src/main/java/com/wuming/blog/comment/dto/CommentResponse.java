package com.wuming.blog.comment.dto;

import com.wuming.blog.comment.entity.Comment;

import java.time.LocalDateTime;

/**
 * 评论响应 DTO。
 *
 * @param id 评论 ID
 * @param content 评论正文
 * @param articleId 所属文章 ID
 * @param authorId 作者 ID
 * @param authorUsername 作者用户名
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record CommentResponse(
        Long id,
        String content,
        Long articleId,
        Long authorId,
        String authorUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将评论实体转换为接口响应。
     */
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getArticle().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
