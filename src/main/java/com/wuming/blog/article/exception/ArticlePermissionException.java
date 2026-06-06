package com.wuming.blog.article.exception;

/**
 * 文章权限异常，非作者编辑或删除文章时抛出。
 */
public class ArticlePermissionException extends RuntimeException {

    public ArticlePermissionException(String message) {
        super(message);
    }
}
