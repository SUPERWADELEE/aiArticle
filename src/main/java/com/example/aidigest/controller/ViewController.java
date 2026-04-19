package com.example.aidigest.controller;

import com.example.aidigest.model.Article;
import com.example.aidigest.repository.ArticleRepository;
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

import java.util.List;

@Controller
public class ViewController {

    private static final int PAGE_SIZE = 20;

    private final ArticleRepository articleRepository;
    private final String siteUrl;
    private final String siteName;
    private final String siteDescription;

    public ViewController(ArticleRepository articleRepository,
                          @Value("${app.site-url}") String siteUrl,
                          @Value("${app.site-name}") String siteName,
                          @Value("${app.site-description}") String siteDescription) {
        this.articleRepository = articleRepository;
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
    public String index(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Article> articlePage = articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(
                PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("currentPage", articlePage.getNumber());
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("pageTitle", siteName);
        model.addAttribute("pageDescription", siteDescription);
        model.addAttribute("canonicalPath", "/");
        model.addAttribute("ogType", "website");
        model.addAttribute("jsonLd", JsonLdBuilder.website(siteName, siteDescription, siteUrl));
        return "index";
    }

    @GetMapping("/articles/{id}")
    public String article(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        String slug = SlugUtil.slugify(article.getTitle());
        model.addAttribute("article", article);
        model.addAttribute("slug", slug);
        model.addAttribute("pageTitle", article.getTitle() + " | " + siteName);
        model.addAttribute("pageDescription", truncate(article.getSummary(), 160));
        model.addAttribute("canonicalPath", "/articles/" + article.getId());
        model.addAttribute("ogType", "article");
        model.addAttribute("jsonLd", JsonLdBuilder.article(article, siteUrl, siteName));
        return "article";
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
