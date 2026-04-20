package com.example.aidigest.service;

import com.example.aidigest.model.Article;
import com.example.aidigest.model.CrawlLog;
import com.example.aidigest.model.CrawlStatus;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.repository.CrawlLogRepository;
import com.example.aidigest.util.AuthorLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DigestService {

    private static final Logger log = LoggerFactory.getLogger(DigestService.class);
    private static final int MAX_ARTICLES_PER_RUN = 15;
    private static final long API_DELAY_MS = 4000;
    private static final int MAX_ARTICLE_AGE_DAYS = 14;
    private static final String FAILED_SUMMARY_MARKER = "摘要生成失敗";
    private static final int ERROR_MESSAGE_MAX_CHARS = 2000;

    private final RssFetchService rssFetchService;
    private final TrendingAiFetcher trendingAiFetcher;
    private final KeywordFilterService keywordFilterService;
    private final GroqSummaryService groqSummaryService;
    private final TelegramService telegramService;
    private final ArticleRepository articleRepository;
    private final CrawlLogRepository crawlLogRepository;
    private final IndexNowService indexNowService;
    private final AuthorLookupService authorLookup;
    private final String siteUrl;

    public DigestService(RssFetchService rssFetchService,
                         TrendingAiFetcher trendingAiFetcher,
                         KeywordFilterService keywordFilterService,
                         GroqSummaryService groqSummaryService,
                         TelegramService telegramService,
                         ArticleRepository articleRepository,
                         CrawlLogRepository crawlLogRepository,
                         IndexNowService indexNowService,
                         AuthorLookupService authorLookup,
                         @Value("${app.site-url}") String siteUrl) {
        this.rssFetchService = rssFetchService;
        this.trendingAiFetcher = trendingAiFetcher;
        this.keywordFilterService = keywordFilterService;
        this.groqSummaryService = groqSummaryService;
        this.telegramService = telegramService;
        this.articleRepository = articleRepository;
        this.crawlLogRepository = crawlLogRepository;
        this.indexNowService = indexNowService;
        this.authorLookup = authorLookup;
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
            List<Article> rssArticles = fetchResult.articles();
            log.info("Fetched {} RSS articles ({} source errors)", rssArticles.size(), fetchResult.errors().size());

            List<Article> trending = trendingAiFetcher.fetchTrending();
            log.info("Fetched {} trending AI articles from Google CSE", trending.size());

            List<Article> fetched = new ArrayList<>(rssArticles.size() + trending.size());
            fetched.addAll(rssArticles);
            fetched.addAll(trending);
            entry.setFetchedCount(fetched.size());
            entry.setSourceErrors(fetchResult.errors().isEmpty() ? null : String.join("\n", fetchResult.errors()));

            List<Article> filtered = keywordFilterService.filter(fetched);
            entry.setFilteredCount(filtered.size());
            log.info("After keyword filter: {} articles", filtered.size());

            Instant ageCutoff = Instant.now().minus(Duration.ofDays(MAX_ARTICLE_AGE_DAYS));
            List<Article> fresh = filtered.stream()
                    .filter(a -> a.getPublishedAt() != null && a.getPublishedAt().isAfter(ageCutoff))
                    .toList();
            log.info("After freshness filter ({}d): {} articles", MAX_ARTICLE_AGE_DAYS, fresh.size());

            List<Article> newArticles = fresh.stream()
                    .filter(a -> !articleRepository.existsByUrl(a.getUrl()))
                    .sorted(Comparator.comparing((Article a) -> authorLookup.find(a).isEmpty())
                            .thenComparing(a -> a.getPublishedAt() == null ? Instant.EPOCH : a.getPublishedAt(),
                                    Comparator.reverseOrder()))
                    .toList();
            entry.setNewCount(newArticles.size());
            log.info("After dedup + priority sort: {} new articles", newArticles.size());

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
