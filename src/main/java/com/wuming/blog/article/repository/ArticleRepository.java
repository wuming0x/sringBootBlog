package com.wuming.blog.article.repository;

import com.wuming.blog.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文章数据访问层，负责 articles 表的基础 CRUD 和分页查询。
 */
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
