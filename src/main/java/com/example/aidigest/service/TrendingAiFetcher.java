package com.example.aidigest.service;

import com.example.aidigest.config.TrendingAiProperties;
import com.example.aidigest.model.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TrendingAiFetcher {

    private static final Logger log = LoggerFactory.getLogger(TrendingAiFetcher.class);

    private final GoogleCseSearchService searchService;
    private final TrendingAiProperties properties;

    public TrendingAiFetcher(GoogleCseSearchService searchService, TrendingAiProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }

    public List<Article> fetchTrending() {
        if (!properties.enabled()) {
            log.debug("Trending AI fetcher disabled");
            return List.of();
        }

        Set<String> seenUrls = new HashSet<>();
        List<Article> results = new ArrayList<>();
        for (String query : properties.queries()) {
            List<GoogleCseSearchService.SearchResult> found =
                    searchService.search(query, properties.resultsPerQuery());
            for (GoogleCseSearchService.SearchResult r : found) {
                if (r.link() == null || !seenUrls.add(r.link())) continue;
                results.add(toArticle(r));
            }
        }
        log.info("Trending AI fetcher collected {} unique articles from {} queries",
                results.size(), properties.queries().size());
        return results;
    }

    private Article toArticle(GoogleCseSearchService.SearchResult r) {
        String source = hostLabel(r.link());
        String content = r.snippet() != null ? r.snippet() : "";
        Instant publishedAt = r.publishedAt() != null ? r.publishedAt() : Instant.now();
        String title = r.title() != null ? r.title() : hostLabel(r.link());
        return new Article(title, r.link(), source, null, content, publishedAt);
    }

    private String hostLabel(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) return "web";
            host = host.startsWith("www.") ? host.substring(4) : host;
            return host;
        } catch (Exception e) {
            return "web";
        }
    }
}
