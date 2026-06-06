package com.wuming.blog.article.exception;

/**
 * 文章请求参数异常，例如标题或正文为空。
 */
public class InvalidArticleRequestException extends RuntimeException {

    public InvalidArticleRequestException(String message) {
        super(message);
    }
}
