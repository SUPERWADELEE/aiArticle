package com.example.aidigest.service;

import com.example.aidigest.config.AppProperties;
import com.example.aidigest.model.Article;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeywordFilterService {

    private final List<String> keywords;

    public KeywordFilterService(AppProperties appProperties) {
        this.keywords = appProperties.keywords().stream()
                .map(String::toLowerCase)
                .toList();
    }

    public List<Article> filter(List<Article> articles) {
        return articles.stream()
                .filter(this::matchesAnyKeyword)
                .toList();
    }

    private boolean matchesAnyKeyword(Article article) {
        String title = article.getTitle() != null ? article.getTitle().toLowerCase() : "";
        String content = article.getContent() != null ? article.getContent().toLowerCase() : "";

        return keywords.stream()
                .anyMatch(keyword -> title.contains(keyword) || content.contains(keyword));
    }
}
