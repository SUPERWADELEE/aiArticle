package com.example.aidigest.service;

import com.example.aidigest.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqSummaryService {

    private static final Logger log = LoggerFactory.getLogger(GroqSummaryService.class);
    private static final String FAILED_SUMMARY = "摘要生成失敗";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public GroqSummaryService(RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.apiKey = appProperties.groq().apiKey();
        this.model = appProperties.groq().model();
        this.apiUrl = appProperties.groq().url();
    }

    public String summarize(String title, String content) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Groq API key not configured, skipping summarization");
            return FAILED_SUMMARY;
        }

        try {
            String prompt = """
                    請用繁體中文用 3 句話摘要以下技術文章，重點放在技術內容和實際應用：
                    標題：%s
                    內容：%s""".formatted(title, truncate(content, 4000));

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 500
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return FAILED_SUMMARY;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) return FAILED_SUMMARY;

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("Failed to summarize article '{}': {}", title, e.getMessage());
            return FAILED_SUMMARY;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
