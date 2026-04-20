package com.example.aidigest.service;

import com.example.aidigest.config.AppProperties;
import com.example.aidigest.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordFilterServiceTest {

    private KeywordFilterService filterService;

    @BeforeEach
    void setUp() {
        List<String> keywords = List.of("claude", "llm", "gpt", "gemini", "ai agent",
                "rag", "fine-tune", "vector", "embedding", "mcp");
        AppProperties props = new AppProperties(null, null, null, keywords);
        filterService = new KeywordFilterService(props);
    }

    @Test
    void matchesKeywordInTitle() {
        Article article = new Article("New Claude Model Released", "https://example.com/1",
                "test", "Some generic content", Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).hasSize(1);
    }

    @Test
    void matchesKeywordInContent() {
        Article article = new Article("Tech Update", "https://example.com/2",
                "test", "Building a RAG pipeline for production", Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).hasSize(1);
    }

    @Test
    void excludesArticleWithNoKeywords() {
        Article article = new Article("JavaScript Framework Update", "https://example.com/3",
                "test", "React 20 is out with new features", Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).isEmpty();
    }

    @Test
    void caseInsensitiveMatching() {
        Article article = new Article("LLM Benchmarks 2026", "https://example.com/4",
                "test", "Testing performance", Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).hasSize(1);
    }

    @Test
    void matchesMultiWordKeyword() {
        Article article = new Article("Building an AI Agent Framework", "https://example.com/5",
                "test", "Some content", Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).hasSize(1);
    }

    @Test
    void handlesNullContent() {
        Article article = new Article("Claude 4 Released", "https://example.com/6",
                "test", null, Instant.now());

        List<Article> result = filterService.filter(List.of(article));

        assertThat(result).hasSize(1);
    }

    @Test
    void filtersMultipleArticles() {
        List<Article> articles = List.of(
                new Article("Claude Update", "https://example.com/7", "test", "content", Instant.now()),
                new Article("Weather Report", "https://example.com/8", "test", "sunny day", Instant.now()),
                new Article("GPT News", "https://example.com/9", "test", "content", Instant.now())
        );

        List<Article> result = filterService.filter(articles);

        assertThat(result).hasSize(2);
    }
}
