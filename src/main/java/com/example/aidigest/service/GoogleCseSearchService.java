package com.example.aidigest.service;

import com.example.aidigest.config.AuthorTrackerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleCseSearchService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCseSearchService.class);
    private static final String ENDPOINT = "https://www.googleapis.com/customsearch/v1";

    // Matches dates like "Apr 3, 2026" or "4 Apr 2026" at the start of a snippet
    private static final Pattern SNIPPET_DATE = Pattern.compile(
            "\\b([A-Za-z]{3,9})\\s+(\\d{1,2}),\\s+(\\d{4})\\b"
    );

    private final RestClient restClient;
    private final AuthorTrackerProperties properties;

    public GoogleCseSearchService(RestClient restClient, AuthorTrackerProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public record SearchResult(String title, String link, String snippet, Instant publishedAt) {}

    public List<SearchResult> search(String query, int maxResults) {
        if (properties.cseApiKey() == null || properties.cseApiKey().isBlank()
                || properties.cseId() == null || properties.cseId().isBlank()) {
            log.warn("Google CSE not configured, skipping search");
            return List.of();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(builder -> builder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/customsearch/v1")
                            .queryParam("q", query)
                            .queryParam("key", properties.cseApiKey())
                            .queryParam("cx", properties.cseId())
                            .queryParam("num", Math.min(maxResults, 10))
                            .queryParam("dateRestrict", "d2")
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) return List.of();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) return List.of();

            List<SearchResult> results = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String title = str(item.get("title"));
                String link = str(item.get("link"));
                String snippet = str(item.get("snippet"));
                Instant publishedAt = extractPublishedAt(item, snippet);
                results.add(new SearchResult(title, link, snippet, publishedAt));
            }
            return results;
        } catch (Exception e) {
            log.warn("CSE search failed for '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Instant extractPublishedAt(Map<String, Object> item, String snippet) {
        Map<String, Object> pagemap = (Map<String, Object>) item.get("pagemap");
        if (pagemap != null) {
            List<Map<String, Object>> metatags = (List<Map<String, Object>>) pagemap.get("metatags");
            if (metatags != null && !metatags.isEmpty()) {
                Map<String, Object> meta = metatags.get(0);
                String iso = firstNonBlank(
                        str(meta.get("article:published_time")),
                        str(meta.get("og:updated_time")),
                        str(meta.get("article:modified_time"))
                );
                if (iso != null) {
                    try {
                        return OffsetDateTime.parse(iso).toInstant();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return parseSnippetDate(snippet);
    }

    private Instant parseSnippetDate(String snippet) {
        if (snippet == null) return null;
        Matcher m = SNIPPET_DATE.matcher(snippet);
        if (!m.find()) return null;
        try {
            String normalized = m.group(1) + " " + m.group(2) + " " + m.group(3);
            LocalDate date = LocalDate.parse(normalized, DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH));
            return date.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
