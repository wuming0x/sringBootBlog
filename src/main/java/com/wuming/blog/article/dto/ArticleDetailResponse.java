package com.wuming.blog.article.dto;

import com.wuming.blog.article.entity.Article;

import java.time.LocalDateTime;

/**
 * 文章详情响应，包含完整正文。
 *
 * @param id 文章 ID
 * @param title 文章标题
 * @param summary 文章摘要
 * @param content 文章正文
 * @param authorId 作者 ID
 * @param authorUsername 作者用户名
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record ArticleDetailResponse(
        Long id,
        String title,
        String summary,
        String content,
        Long authorId,
        String authorUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将文章实体转换为详情响应。
     */
    public static ArticleDetailResponse from(Article article) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getContent(),
                article.getAuthor().getId(),
                article.getAuthor().getUsername(),
                article.getCreatedAt(),
                article.getUpdatedAt()
        );
    }
}
