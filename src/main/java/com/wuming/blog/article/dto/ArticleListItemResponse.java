package com.wuming.blog.article.dto;

import com.wuming.blog.article.entity.Article;

import java.time.LocalDateTime;

/**
 * 文章分页列表项响应，不包含完整正文。
 *
 * @param id 文章 ID
 * @param title 文章标题
 * @param summary 文章摘要
 * @param authorId 作者 ID
 * @param authorUsername 作者用户名
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record ArticleListItemResponse(
        Long id,
        String title,
        String summary,
        Long authorId,
        String authorUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将文章实体转换为分页列表项响应。
     */
    public static ArticleListItemResponse from(Article article) {
        return new ArticleListItemResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getAuthor().getId(),
                article.getAuthor().getUsername(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
