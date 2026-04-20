package com.example.aidigest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Groq groq,
        Telegram telegram,
        Rss rss,
        List<String> keywords
) {
    public record Groq(String apiKey, String model, String url) {}
    public record Telegram(String botToken, String chatId, String url) {}
    public record Rss(List<String> sources) {}
}
