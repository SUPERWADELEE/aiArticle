package com.example.aidigest.controller;

import com.example.aidigest.model.Article;
import com.example.aidigest.model.ArticleDto;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.service.DigestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DigestController {

    private final DigestService digestService;
    private final ArticleRepository articleRepository;

    public DigestController(DigestService digestService, ArticleRepository articleRepository) {
        this.digestService = digestService;
        this.articleRepository = articleRepository;
    }

    @GetMapping("/fetch")
    public List<ArticleDto> fetch() {
        List<Article> articles = digestService.runDigest("manual");
        return articles.stream().map(ArticleDto::from).toList();
    }

    @GetMapping("/articles")
    public List<ArticleDto> articles() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ArticleDto::from)
                .toList();
    }
}
