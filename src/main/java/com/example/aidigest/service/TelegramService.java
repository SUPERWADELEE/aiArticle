package com.example.aidigest.service;

import com.example.aidigest.config.AppProperties;
import com.example.aidigest.model.Article;
import com.example.aidigest.util.AuthorLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final int MAX_MESSAGE_LENGTH = 4096;

    private final RestClient restClient;
    private final AuthorLookupService authorLookup;
    private final String botToken;
    private final String chatId;
    private final String baseUrl;

    public TelegramService(RestClient restClient, AuthorLookupService authorLookup, AppProperties appProperties) {
        this.restClient = restClient;
        this.authorLookup = authorLookup;
        this.botToken = appProperties.telegram().botToken();
        this.chatId = appProperties.telegram().chatId();
        this.baseUrl = appProperties.telegram().url();
    }

    public void sendDigest(List<Article> articles) {
        if (articles.isEmpty()) {
            log.info("No articles to send, skipping Telegram notification");
            return;
        }

        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram bot token or chat ID not configured, skipping notification");
            return;
        }

        String fullMessage = formatDigest(articles);
        List<String> chunks = splitMessage(fullMessage);

        for (String chunk : chunks) {
            sendMessage(chunk);
        }

        log.info("Sent digest with {} articles in {} message(s)", articles.size(), chunks.size());
    }

    String formatDigest(List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("*\uD83D\uDCF0 AI 每日技術摘要*\n\n");

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            sb.append("*").append(i + 1).append(". ").append(escapeMarkdown(article.getTitle())).append("*\n");
            sb.append("\u270D\uFE0F _by ").append(escapeMarkdown(authorLookup.displayWithTitle(article))).append("_\n");
            sb.append(escapeMarkdown(article.getSummary())).append("\n");
            sb.append("[閱讀原文](").append(article.getUrl()).append(")\n\n");
        }

        return sb.toString().trim();
    }

    List<String> splitMessage(String message) {
        List<String> chunks = new ArrayList<>();
        if (message.length() <= MAX_MESSAGE_LENGTH) {
            chunks.add(message);
            return chunks;
        }

        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + MAX_MESSAGE_LENGTH, message.length());
            if (end < message.length()) {
                int lastNewline = message.lastIndexOf("\n\n", end);
                if (lastNewline > start) {
                    end = lastNewline;
                }
            }
            chunks.add(message.substring(start, end).trim());
            start = end;
        }

        return chunks;
    }

    private void sendMessage(String text) {
        try {
            String url = baseUrl + "/bot" + botToken + "/sendMessage";
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "chat_id", chatId,
                            "text", text,
                            "parse_mode", "Markdown",
                            "disable_web_page_preview", true
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
        }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("~", "\\~")
                .replace("`", "\\`");
    }
}
