package com.example.aidigest.service;

import com.example.aidigest.model.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.aidigest.config.AppProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class RssFetchService {

    private static final Logger log = LoggerFactory.getLogger(RssFetchService.class);

    private final Executor executor;
    private final List<String> sources;

    public RssFetchService(
            @Qualifier("rssFetchExecutor") Executor executor,
            AppProperties appProperties) {
        this.executor = executor;
        this.sources = appProperties.rss().sources();
    }

    public record SourceOutcome(List<Article> articles, String error) {
        static SourceOutcome ok(List<Article> articles) {
            return new SourceOutcome(articles, null);
        }
        static SourceOutcome fail(String error) {
            return new SourceOutcome(List.of(), error);
        }
    }

    public record FetchResult(List<Article> articles, List<String> errors) {}

    public List<Article> fetchAll() {
        return fetchAllWithErrors().articles();
    }

    public FetchResult fetchAllWithErrors() {
        List<CompletableFuture<SourceOutcome>> futures = sources.stream()
                .map(source -> CompletableFuture.supplyAsync(() -> fetchSource(source), executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<Article> all = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            SourceOutcome outcome = futures.get(i).join();
            all.addAll(outcome.articles());
            if (outcome.error() != null) {
                errors.add(sources.get(i) + " → " + outcome.error());
            }
        }
        return new FetchResult(all, errors);
    }

    SourceOutcome fetchSource(String sourceUrl) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(URI.create(sourceUrl).toURL()));
            String sourceName = feed.getTitle() != null ? feed.getTitle() : sourceUrl;

            List<Article> articles = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();
                if (title == null || link == null) continue;

                String content = "";
                if (entry.getDescription() != null) {
                    content = entry.getDescription().getValue();
                } else if (!entry.getContents().isEmpty()) {
                    content = entry.getContents().get(0).getValue();
                }

                Instant publishedAt = entry.getPublishedDate() != null
                        ? entry.getPublishedDate().toInstant()
                        : Instant.now();

                articles.add(new Article(title, link, sourceName, content, publishedAt));
            }

            log.info("Fetched {} articles from {}", articles.size(), sourceName);
            return SourceOutcome.ok(articles);
        } catch (Exception e) {
            log.error("Failed to fetch RSS from {}: {}", sourceUrl, e.getMessage());
            return SourceOutcome.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
