package com.wuming.blog.comment.exception;

/**
 * 评论请求参数异常。
 */
public class InvalidCommentRequestException extends RuntimeException {

    public InvalidCommentRequestException(String message) {
        super(message);
    }
}
