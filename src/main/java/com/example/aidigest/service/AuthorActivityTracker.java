package com.example.aidigest.service;

import com.example.aidigest.config.AuthorTrackerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorActivityTracker {

    private static final Logger log = LoggerFactory.getLogger(AuthorActivityTracker.class);

    private final GoogleCseSearchService searchService;
    private final AuthorTrackerProperties properties;

    public AuthorActivityTracker(GoogleCseSearchService searchService,
                                 AuthorTrackerProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.enabled();
    }

    public record AuthorActivity(String handle, String name, String title,
                                 List<GoogleCseSearchService.SearchResult> findings) {}

    public record Report(Instant generatedAt, int totalAuthors, int authorsWithActivity,
                         List<AuthorActivity> details) {}

    public Report runDailyCheck() {
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofHours(properties.lookbackHours()));

        List<AuthorActivity> details = new ArrayList<>();
        int authorsWithActivity = 0;

        for (AuthorTrackerProperties.Handle h : properties.handles()) {
            List<GoogleCseSearchService.SearchResult> findings = searchForHandle(h, cutoff);
            if (!findings.isEmpty()) {
                authorsWithActivity++;
            }
            details.add(new AuthorActivity(h.handle(), h.name(), h.title(), findings));
        }

        log.info("Author activity check: {}/{} authors with new activity in last {}h",
                authorsWithActivity, properties.handles().size(), properties.lookbackHours());

        return new Report(now, properties.handles().size(), authorsWithActivity, details);
    }

    private List<GoogleCseSearchService.SearchResult> searchForHandle(
            AuthorTrackerProperties.Handle h, Instant cutoff) {
        String query = "site:x.com \"@" + h.handle() + "\" OR \"" + h.name() + "\"";
        List<GoogleCseSearchService.SearchResult> results = searchService.search(query, 5);

        List<GoogleCseSearchService.SearchResult> recent = new ArrayList<>();
        for (GoogleCseSearchService.SearchResult r : results) {
            if (r.publishedAt() != null && r.publishedAt().isAfter(cutoff)) {
                recent.add(r);
            }
        }
        return recent;
    }
}
