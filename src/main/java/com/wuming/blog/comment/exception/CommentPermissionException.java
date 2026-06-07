package com.wuming.blog.comment.exception;

/**
 * 评论权限异常，非评论作者或文章作者删除评论时抛出。
 */
public class CommentPermissionException extends RuntimeException {

    public CommentPermissionException(String message) {
        super(message);
    }
}
