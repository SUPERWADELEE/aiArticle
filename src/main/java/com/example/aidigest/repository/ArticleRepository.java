package com.example.aidigest.repository;

import com.example.aidigest.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByUrl(String url);

    List<Article> findAllByOrderByCreatedAtDesc();

    List<Article> findBySourceOrderByPublishedAtDescCreatedAtDesc(String source);

    Page<Article> findAllByOrderByPublishedAtDescCreatedAtDesc(Pageable pageable);

    @Query("SELECT DISTINCT a.source FROM Article a ORDER BY a.source")
    List<String> findDistinctSources();
}
