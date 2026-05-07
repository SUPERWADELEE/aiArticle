package com.example.aidigest.controller;

import com.example.aidigest.config.AuthorTrackerProperties;
import com.example.aidigest.config.AuthorTrackerProperties.Handle;
import com.example.aidigest.model.Article;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.util.AuthorLookupService;
import com.example.aidigest.util.JsonLdBuilder;
import com.example.aidigest.util.SlugUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class ViewController {

    private static final int PAGE_SIZE = 20;

    private final ArticleRepository articleRepository;
    private final AuthorLookupService authorLookup;
    private final AuthorTrackerProperties trackerProperties;
    private final String siteUrl;
    private final String siteName;
    private final String siteDescription;

    public ViewController(ArticleRepository articleRepository,
                          AuthorLookupService authorLookup,
                          AuthorTrackerProperties trackerProperties,
                          @Value("${app.site-url}") String siteUrl,
                          @Value("${app.site-name}") String siteName,
                          @Value("${app.site-description}") String siteDescription) {
        this.articleRepository = articleRepository;
        this.authorLookup = authorLookup;
        this.trackerProperties = trackerProperties;
        this.siteUrl = siteUrl;
        this.siteName = siteName;
        this.siteDescription = siteDescription;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("siteUrl", siteUrl);
        model.addAttribute("siteName", siteName);
        model.addAttribute("siteDescription", siteDescription);
        model.addAttribute("sources", articleRepository.findDistinctSources());
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/feed";
    }

    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        String slug = SlugUtil.slugify(article.getTitle());
        String authorHandle = authorLookup.find(article)
                .map(h -> h.handle().toLowerCase(Locale.ROOT))
                .orElse(null);
        model.addAttribute("article", article);
        model.addAttribute("slug", slug);
        model.addAttribute("authorHandle", authorHandle);
        model.addAttribute("authorDisplay", authorLookup.displayWithTitle(article));
        model.addAttribute("pageTitle", article.getTitle() + " | " + siteName);
        model.addAttribute("pageDescription", truncate(article.getSummary(), 160));
        model.addAttribute("canonicalPath", "/articles/" + article.getId());
        model.addAttribute("ogType", "article");
        model.addAttribute("jsonLd", JsonLdBuilder.article(article, siteUrl, siteName));
        return "article";
    }

    @GetMapping("/feed")
    public String feed(@RequestParam(defaultValue = "0") int page, Model model) {
        int safePage = Math.max(page, 0);
        Page<Article> articlePage = articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(
                PageRequest.of(safePage, PAGE_SIZE));
        List<Article> articles = articlePage.getContent();

        List<Map<String, Object>> topAuthors = computeTopAuthors(articles, 5);

        Map<String, Object> feedStats = new LinkedHashMap<>();
        feedStats.put("totalArticles", articleRepository.count());
        feedStats.put("authorsTracked", trackerProperties.handles().size());
        feedStats.put("lastSync", DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.UTC).format(Instant.now()) + " UTC");

        model.addAttribute("articles", articles);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("topAuthors", topAuthors);
        model.addAttribute("feedStats", feedStats);
        model.addAttribute("recentThreshold", Instant.now().minusSeconds(24 * 60 * 60));
        String description = "AI_FEED — " + siteDescription;
        model.addAttribute("pageTitle", "AI_FEED | " + siteName);
        model.addAttribute("pageDescription", description);
        model.addAttribute("canonicalPath", "/feed");
        model.addAttribute("ogType", "website");
        model.addAttribute("jsonLd", JsonLdBuilder.website("AI_FEED", description, siteUrl + "/feed"));
        return "feed";
    }

    @GetMapping("/authors/{handle}")
    public String authorProfile(@PathVariable String handle, Model model) {
        Handle author = authorLookup.findByHandle(handle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        // optimize when articles exceed 5000
        List<Article> recent = articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(
                PageRequest.of(0, 500)).getContent();
        List<Article> authorArticles = authorLookup.articlesFor(author, recent);

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        long articlesThisMonth = authorArticles.stream()
                .filter(a -> {
                    Instant published = a.getPublishedAt();
                    if (published == null) return false;
                    ZonedDateTime z = published.atZone(ZoneOffset.UTC);
                    return z.getYear() == nowUtc.getYear() && z.getMonth() == nowUtc.getMonth();
                })
                .count();

        List<Handle> relatedAuthors = trackerProperties.handles().stream()
                .filter(h -> !h.handle().equalsIgnoreCase(author.handle()))
                .limit(3)
                .toList();

        String lowerHandle = author.handle().toLowerCase(Locale.ROOT);
        String description = author.name() + " 是 " + author.title() + "。在 " + siteName + " 上追蹤其文章與動態。";
        String canonicalPath = "/authors/" + lowerHandle;

        model.addAttribute("author", author);
        model.addAttribute("authorPath", "~/authors/" + lowerHandle);
        model.addAttribute("authorArticles", authorArticles);
        model.addAttribute("articlesThisMonth", articlesThisMonth);
        model.addAttribute("relatedAuthors", relatedAuthors);
        model.addAttribute("pageTitle", author.name() + " | " + siteName);
        model.addAttribute("pageDescription", description);
        model.addAttribute("canonicalPath", canonicalPath);
        model.addAttribute("ogType", "profile");
        model.addAttribute("jsonLd",
                JsonLdBuilder.person(author.name(), author.title(), siteUrl + canonicalPath));
        return "author-profile";
    }

    private List<Map<String, Object>> computeTopAuthors(List<Article> articles, int limit) {
        Map<String, int[]> counts = new HashMap<>();
        Map<String, Handle> handleCache = new HashMap<>();
        for (Article a : articles) {
            Optional<Handle> hit = authorLookup.find(a);
            if (hit.isEmpty()) continue;
            String key = hit.get().handle().toLowerCase(Locale.ROOT);
            counts.computeIfAbsent(key, k -> new int[]{0})[0]++;
            handleCache.putIfAbsent(key, hit.get());
        }
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, int[]>>comparingInt(e -> e.getValue()[0]).reversed())
                .limit(limit)
                .map(e -> {
                    Handle h = handleCache.get(e.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("handle", h.handle().toLowerCase(Locale.ROOT));
                    row.put("name", h.name());
                    row.put("title", h.title());
                    row.put("articleCount", e.getValue()[0]);
                    return row;
                })
                .toList();
    }

    @GetMapping("/sources/{source}")
    public String source(@PathVariable String source, Model model) {
        List<Article> articles = articleRepository.findBySourceOrderByPublishedAtDescCreatedAtDesc(source);
        if (articles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source not found");
        }
        model.addAttribute("source", source);
        model.addAttribute("articles", articles);
        model.addAttribute("pageTitle", source + " 文章 | " + siteName);
        String description = "來自 " + source + " 的 AI/LLM 技術文章中文摘要";
        model.addAttribute("pageDescription", description);
        model.addAttribute("canonicalPath", "/sources/" + source);
        model.addAttribute("ogType", "website");
        model.addAttribute("jsonLd", JsonLdBuilder.collectionPage(
                source + " 文章", description, siteUrl + "/sources/" + source));
        return "source-list";
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max - 1) + "…";
    }
}
