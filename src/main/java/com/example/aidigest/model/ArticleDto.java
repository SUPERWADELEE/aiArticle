package com.example.aidigest.model;

import java.time.Instant;

public record ArticleDto(
        Long id,
        String title,
        String url,
        String source,
        String author,
        String summary,
        Instant publishedAt,
        Instant createdAt
) {
    public static ArticleDto from(Article article) {
        return new ArticleDto(
                article.getId(),
                article.getTitle(),
                article.getUrl(),
                article.getSource(),
                article.getAuthor(),
                article.getSummary(),
                article.getPublishedAt(),
                article.getCreatedAt()
        );
    }
}
