package com.wuming.blog.comment.exception;

/**
 * 评论不存在异常。
 */
public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(String message) {
        super(message);
    }
}
