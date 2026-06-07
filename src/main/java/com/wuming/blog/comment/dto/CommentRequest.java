package com.wuming.blog.comment.dto;

/**
 * 评论发布请求体。
 *
 * @param content 评论正文
 */
public record CommentRequest(String content) {
}
