package com.example.aidigest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.trending-ai")
public record TrendingAiProperties(
        boolean enabled,
        List<String> queries,
        int resultsPerQuery
) {
    public TrendingAiProperties {
        if (queries == null || queries.isEmpty()) {
            queries = List.of(
                    "LLM OR \"large language model\" announcement",
                    "\"AI agent\" OR \"agentic AI\"",
                    "GPT-5 OR Claude OR Gemini release",
                    "\"open source LLM\" OR \"open weights\"",
                    "AI research paper"
            );
        }
        if (resultsPerQuery <= 0) resultsPerQuery = 5;
    }
}
