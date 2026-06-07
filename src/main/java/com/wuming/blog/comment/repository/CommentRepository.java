package com.wuming.blog.comment.repository;

import com.wuming.blog.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 评论数据访问层，负责 comments 表的基础 CRUD 和按文章分页查询。
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据文章 ID 分页查询评论。
     */
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);
}
