package com.example.aidigest.controller;

import com.example.aidigest.model.Article;
import com.example.aidigest.repository.ArticleRepository;
import com.example.aidigest.service.IndexNowService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class SeoController {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private static final int FEED_ITEM_LIMIT = 50;

    private final ArticleRepository articleRepository;
    private final IndexNowService indexNowService;
    private final String siteUrl;
    private final String siteName;
    private final String siteDescription;

    public SeoController(ArticleRepository articleRepository,
                         IndexNowService indexNowService,
                         @Value("${app.site-url}") String siteUrl,
                         @Value("${app.site-name}") String siteName,
                         @Value("${app.site-description}") String siteDescription) {
        this.articleRepository = articleRepository;
        this.indexNowService = indexNowService;
        this.siteUrl = siteUrl;
        this.siteName = siteName;
        this.siteDescription = siteDescription;
    }

    @GetMapping(value = "/{key:[A-Za-z0-9]{8,128}}.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String indexNowKey(@PathVariable String key) {
        if (!indexNowService.isKeyConfigured() || !key.equals(indexNowService.getKey())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return key;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<Article> articles = articleRepository.findAllByOrderByCreatedAtDesc();
        List<String> sources = articleRepository.findDistinctSources();
        String today = DATE.format(Instant.now());

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendUrl(sb, siteUrl + "/", today, "daily", "1.0");
        for (String src : sources) {
            String encoded = URLEncoder.encode(src, StandardCharsets.UTF_8).replace("+", "%20");
            appendUrl(sb, siteUrl + "/sources/" + encoded, today, "daily", "0.7");
        }
        for (Article a : articles) {
            String lastmod = a.getCreatedAt() != null ? DATE.format(a.getCreatedAt()) : today;
            appendUrl(sb, siteUrl + "/articles/" + a.getId(), lastmod, "monthly", "0.8");
        }

        sb.append("</urlset>\n");
        return sb.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return "User-agent: *\nAllow: /\nDisallow: /api/\n\nSitemap: " + siteUrl + "/sitemap.xml\n";
    }

    @GetMapping(value = "/feed.xml", produces = "application/atom+xml;charset=UTF-8")
    @ResponseBody
    public String feed() {
        List<Article> articles = articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(
                PageRequest.of(0, FEED_ITEM_LIMIT)).getContent();

        String updated = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xml:lang=\"zh-Hant\">\n");
        sb.append("  <title>").append(xmlEscape(siteName)).append("</title>\n");
        sb.append("  <subtitle>").append(xmlEscape(siteDescription)).append("</subtitle>\n");
        sb.append("  <link href=\"").append(xmlEscape(siteUrl)).append("/\" rel=\"alternate\"/>\n");
        sb.append("  <link href=\"").append(xmlEscape(siteUrl)).append("/feed.xml\" rel=\"self\"/>\n");
        sb.append("  <id>").append(xmlEscape(siteUrl)).append("/</id>\n");
        sb.append("  <updated>").append(updated).append("</updated>\n");

        for (Article a : articles) {
            String articleUrl = siteUrl + "/articles/" + a.getId();
            Instant published = a.getPublishedAt() != null ? a.getPublishedAt() : a.getCreatedAt();
            sb.append("  <entry>\n");
            sb.append("    <title>").append(xmlEscape(a.getTitle())).append("</title>\n");
            sb.append("    <link href=\"").append(xmlEscape(articleUrl)).append("\"/>\n");
            sb.append("    <id>").append(xmlEscape(articleUrl)).append("</id>\n");
            if (published != null) {
                sb.append("    <updated>").append(DateTimeFormatter.ISO_INSTANT.format(published)).append("</updated>\n");
                sb.append("    <published>").append(DateTimeFormatter.ISO_INSTANT.format(published)).append("</published>\n");
            }
            if (a.getSource() != null) {
                sb.append("    <author><name>").append(xmlEscape(a.getSource())).append("</name></author>\n");
            }
            if (a.getSummary() != null) {
                sb.append("    <summary type=\"text\">").append(xmlEscape(a.getSummary())).append("</summary>\n");
            }
            sb.append("  </entry>\n");
        }

        sb.append("</feed>\n");
        return sb.toString();
    }

    private static void appendUrl(StringBuilder sb, String loc, String lastmod, String changefreq, String priority) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(xmlEscape(loc)).append("</loc>\n");
        sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }

    private static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
