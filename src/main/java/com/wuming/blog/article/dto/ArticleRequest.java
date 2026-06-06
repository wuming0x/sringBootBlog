package com.wuming.blog.article.dto;

/**
 * 创建或编辑文章请求参数。
 *
 * @param title 文章标题
 * @param summary 文章摘要，可为空
 * @param content 文章正文
 */
public record ArticleRequest(
        String title,
        String summary,
        String content
) {
}
