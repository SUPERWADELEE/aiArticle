package com.example.aidigest.service;

import com.example.aidigest.model.Article;
import com.example.aidigest.model.CrawlLog;
import com.example.aidigest.model.CrawlStatus;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.repository.CrawlLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DigestService {

    private static final Logger log = LoggerFactory.getLogger(DigestService.class);
    private static final int MAX_ARTICLES_PER_RUN = 15;
    private static final long API_DELAY_MS = 4000;
    private static final String FAILED_SUMMARY_MARKER = "摘要生成失敗";
    private static final int ERROR_MESSAGE_MAX_CHARS = 2000;

    private final RssFetchService rssFetchService;
    private final KeywordFilterService keywordFilterService;
    private final GroqSummaryService groqSummaryService;
    private final TelegramService telegramService;
    private final ArticleRepository articleRepository;
    private final CrawlLogRepository crawlLogRepository;
    private final IndexNowService indexNowService;
    private final String siteUrl;

    public DigestService(RssFetchService rssFetchService,
                         KeywordFilterService keywordFilterService,
                         GroqSummaryService groqSummaryService,
                         TelegramService telegramService,
                         ArticleRepository articleRepository,
                         CrawlLogRepository crawlLogRepository,
                         IndexNowService indexNowService,
                         @Value("${app.site-url}") String siteUrl) {
        this.rssFetchService = rssFetchService;
        this.keywordFilterService = keywordFilterService;
        this.groqSummaryService = groqSummaryService;
        this.telegramService = telegramService;
        this.articleRepository = articleRepository;
        this.crawlLogRepository = crawlLogRepository;
        this.indexNowService = indexNowService;
        this.siteUrl = siteUrl;
    }

    public List<Article> runDigest() {
        return runDigest("manual");
    }

    public List<Article> runDigest(String triggeredBy) {
        log.info("Starting digest process (triggeredBy={})", triggeredBy);

        CrawlLog entry = new CrawlLog();
        entry.setStartedAt(Instant.now());
        entry.setStatus(CrawlStatus.RUNNING);
        entry.setTriggeredBy(triggeredBy);
        entry = crawlLogRepository.save(entry);

        try {
            RssFetchService.FetchResult fetchResult = rssFetchService.fetchAllWithErrors();
            List<Article> fetched = fetchResult.articles();
            entry.setFetchedCount(fetched.size());
            entry.setSourceErrors(fetchResult.errors().isEmpty() ? null : String.join("\n", fetchResult.errors()));
            log.info("Fetched {} articles ({} source errors)", fetched.size(), fetchResult.errors().size());

            List<Article> filtered = keywordFilterService.filter(fetched);
            entry.setFilteredCount(filtered.size());
            log.info("After keyword filter: {} articles", filtered.size());

            List<Article> newArticles = filtered.stream()
                    .filter(a -> !articleRepository.existsByUrl(a.getUrl()))
                    .toList();
            entry.setNewCount(newArticles.size());
            log.info("After deduplication: {} new articles", newArticles.size());

            if (newArticles.isEmpty()) {
                entry.setSavedCount(0);
                entry.setSummarizeFailedCount(0);
                entry.setStatus(fetchResult.errors().isEmpty() ? CrawlStatus.SUCCESS : CrawlStatus.PARTIAL);
                log.info("No new articles to process");
                return List.of();
            }

            List<Article> toProcess = newArticles.stream()
                    .limit(MAX_ARTICLES_PER_RUN)
                    .toList();
            if (newArticles.size() > MAX_ARTICLES_PER_RUN) {
                log.info("Limiting to {} articles (skipped {})", MAX_ARTICLES_PER_RUN, newArticles.size() - MAX_ARTICLES_PER_RUN);
            }

            List<Article> summarized = new ArrayList<>();
            for (int i = 0; i < toProcess.size(); i++) {
                Article article = toProcess.get(i);
                String summary = groqSummaryService.summarize(article.getTitle(), article.getContent());
                article.setSummary(summary);
                summarized.add(article);
                if (i < toProcess.size() - 1) {
                    try {
                        Thread.sleep(API_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            int failedSummaries = (int) summarized.stream()
                    .filter(a -> FAILED_SUMMARY_MARKER.equals(a.getSummary()))
                    .count();
            entry.setSummarizeFailedCount(failedSummaries);

            List<Article> saved = articleRepository.saveAll(summarized);
            entry.setSavedCount(saved.size());
            log.info("Saved {} articles ({} summary failures)", saved.size(), failedSummaries);

            telegramService.sendDigest(saved);

            List<String> newUrls = saved.stream()
                    .map(a -> siteUrl + "/articles/" + a.getId())
                    .toList();
            indexNowService.ping(newUrls);

            if (!fetchResult.errors().isEmpty() || failedSummaries > 0) {
                entry.setStatus(CrawlStatus.PARTIAL);
            } else {
                entry.setStatus(CrawlStatus.SUCCESS);
            }

            log.info("Digest process complete (status={})", entry.getStatus());
            return saved;
        } catch (Exception e) {
            log.error("Digest run failed", e);
            entry.setStatus(CrawlStatus.FAILED);
            entry.setErrorMessage(summarizeError(e));
            throw e;
        } finally {
            entry.setFinishedAt(Instant.now());
            entry.setDurationMs(Duration.between(entry.getStartedAt(), entry.getFinishedAt()).toMillis());
            crawlLogRepository.save(entry);
        }
    }

    private String summarizeError(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() <= ERROR_MESSAGE_MAX_CHARS ? trace : trace.substring(0, ERROR_MESSAGE_MAX_CHARS) + "…";
    }
}
