package com.example.aidigest.util;

import com.example.aidigest.config.AuthorTrackerProperties;
import com.example.aidigest.model.Article;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component("authorLookup")
public class AuthorLookupService {

    private final Map<String, AuthorTrackerProperties.Handle> byHandle = new HashMap<>();
    private final Map<String, AuthorTrackerProperties.Handle> byNameLower = new HashMap<>();
    private final Map<String, AuthorTrackerProperties.Handle> byLastNameLower = new HashMap<>();

    public AuthorLookupService(AuthorTrackerProperties properties) {
        for (AuthorTrackerProperties.Handle h : properties.handles()) {
            byHandle.put(h.handle().toLowerCase(Locale.ROOT), h);
            byNameLower.put(h.name().toLowerCase(Locale.ROOT), h);
            String[] parts = h.name().split("\\s+");
            if (parts.length > 1) {
                byLastNameLower.put(parts[parts.length - 1].toLowerCase(Locale.ROOT), h);
            }
        }
    }

    public Optional<AuthorTrackerProperties.Handle> find(Article article) {
        if (article == null) return Optional.empty();
        Optional<AuthorTrackerProperties.Handle> hit = matchAgainst(article.getAuthor());
        if (hit.isPresent()) return hit;
        return matchAgainst(article.getSource());
    }

    private Optional<AuthorTrackerProperties.Handle> matchAgainst(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String lower = text.toLowerCase(Locale.ROOT);
        if (byHandle.containsKey(lower)) return Optional.of(byHandle.get(lower));
        if (byNameLower.containsKey(lower)) return Optional.of(byNameLower.get(lower));
        for (Map.Entry<String, AuthorTrackerProperties.Handle> e : byNameLower.entrySet()) {
            if (lower.contains(e.getKey())) return Optional.of(e.getValue());
        }
        for (Map.Entry<String, AuthorTrackerProperties.Handle> e : byHandle.entrySet()) {
            if (lower.contains(e.getKey())) return Optional.of(e.getValue());
        }
        for (Map.Entry<String, AuthorTrackerProperties.Handle> e : byLastNameLower.entrySet()) {
            if (lower.contains(e.getKey())) return Optional.of(e.getValue());
        }
        return Optional.empty();
    }

    public String displayWithTitle(Article article) {
        Optional<AuthorTrackerProperties.Handle> handle = find(article);
        if (handle.isPresent()) {
            AuthorTrackerProperties.Handle h = handle.get();
            return h.name() + "（" + h.title() + "）";
        }
        if (article == null) return "";
        if (article.getAuthor() != null && !article.getAuthor().isBlank()) return article.getAuthor();
        String source = article.getSource();
        return source == null ? "" : source;
    }
}
