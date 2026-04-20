package com.example.aidigest.controller;

import com.example.aidigest.model.Article;
import com.example.aidigest.model.ArticleDto;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.service.AuthorActivityTracker;
import com.example.aidigest.service.DigestService;
import com.example.aidigest.service.TelegramService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DigestController {

    private final DigestService digestService;
    private final ArticleRepository articleRepository;
    private final AuthorActivityTracker authorActivityTracker;
    private final TelegramService telegramService;

    public DigestController(DigestService digestService,
                            ArticleRepository articleRepository,
                            AuthorActivityTracker authorActivityTracker,
                            TelegramService telegramService) {
        this.digestService = digestService;
        this.articleRepository = articleRepository;
        this.authorActivityTracker = authorActivityTracker;
        this.telegramService = telegramService;
    }

    @GetMapping("/fetch")
    public List<ArticleDto> fetch() {
        List<Article> articles = digestService.runDigest("manual");
        return articles.stream().map(ArticleDto::from).toList();
    }

    @GetMapping("/author-check")
    public Map<String, Object> authorCheck() {
        if (!authorActivityTracker.isEnabled()) {
            return Map.of("enabled", false, "message", "Set AUTHOR_TRACKER_ENABLED=true and configure GOOGLE_CSE_API_KEY + GOOGLE_CSE_ID");
        }
        AuthorActivityTracker.Report report = authorActivityTracker.runDailyCheck();
        telegramService.sendAuthorActivityReport(report);
        return Map.of(
                "enabled", true,
                "generatedAt", report.generatedAt(),
                "totalAuthors", report.totalAuthors(),
                "authorsWithActivity", report.authorsWithActivity(),
                "details", report.details()
        );
    }

    @GetMapping("/articles")
    public List<ArticleDto> articles() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ArticleDto::from)
                .toList();
    }
}
