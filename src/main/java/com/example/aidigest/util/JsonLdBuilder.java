package com.example.aidigest.util;

import com.example.aidigest.model.Article;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonLdBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonLdBuilder() {}

    public static String website(String name, String description, String url) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("@context", "https://schema.org");
        root.put("@type", "WebSite");
        root.put("name", name);
        root.put("description", description);
        root.put("url", url);
        return write(root);
    }

    public static String article(Article article, String siteUrl, String siteName) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("@context", "https://schema.org");
        root.put("@type", "Article");
        root.put("headline", article.getTitle());
        root.put("description", article.getSummary() == null ? "" : article.getSummary());
        if (article.getPublishedAt() != null) {
            root.put("datePublished", article.getPublishedAt().toString());
        }
        root.put("dateModified", (article.getCreatedAt() == null ? Instant.now() : article.getCreatedAt()).toString());

        Map<String, Object> mainEntity = new LinkedHashMap<>();
        mainEntity.put("@type", "WebPage");
        mainEntity.put("@id", siteUrl + "/articles/" + article.getId());
        root.put("mainEntityOfPage", mainEntity);

        Map<String, Object> publisher = new LinkedHashMap<>();
        publisher.put("@type", "Organization");
        publisher.put("name", siteName);
        root.put("publisher", publisher);

        Map<String, Object> author = new LinkedHashMap<>();
        author.put("@type", "Organization");
        author.put("name", article.getSource());
        root.put("author", author);

        if (article.getUrl() != null) {
            root.put("url", article.getUrl());
        }

        return write(root);
    }

    public static String collectionPage(String name, String description, String url) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("@context", "https://schema.org");
        root.put("@type", "CollectionPage");
        root.put("name", name);
        root.put("description", description);
        root.put("url", url);
        return write(root);
    }

    private static String write(Map<String, Object> data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
