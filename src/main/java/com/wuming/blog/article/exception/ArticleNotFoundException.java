package com.wuming.blog.article.exception;

/**
 * 文章不存在异常，根据文章 ID 查询不到数据时抛出。
 */
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String message) {
        super(message);
    }
}
