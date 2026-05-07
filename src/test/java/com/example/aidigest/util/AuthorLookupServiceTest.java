package com.example.aidigest.util;

import com.example.aidigest.config.AuthorTrackerProperties;
import com.example.aidigest.config.AuthorTrackerProperties.Handle;
import com.example.aidigest.model.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorLookupServiceTest {

    private AuthorLookupService service;
    private Handle sundar;
    private Handle sam;
    private Handle yann;

    @BeforeEach
    void setUp() {
        sundar = new Handle("sundar-pichai", "Sundar Pichai", "Google CEO");
        sam = new Handle("sama", "Sam Altman", "OpenAI CEO");
        yann = new Handle("ylecun", "Yann LeCun", "Meta Chief AI Scientist");
        AuthorTrackerProperties props = new AuthorTrackerProperties(
                true, null, null, 48, List.of(sundar, sam, yann));
        service = new AuthorLookupService(props);
    }

    @Test
    void findByHandleReturnsHandleForExactMatch() {
        Optional<Handle> hit = service.findByHandle("sundar-pichai");
        assertThat(hit).contains(sundar);
    }

    @Test
    void findByHandleIsCaseInsensitive() {
        Optional<Handle> hit = service.findByHandle("Sundar-Pichai");
        assertThat(hit).contains(sundar);
    }

    @Test
    void findByHandleReturnsEmptyForNullOrBlank() {
        assertThat(service.findByHandle(null)).isEmpty();
        assertThat(service.findByHandle("")).isEmpty();
        assertThat(service.findByHandle("   ")).isEmpty();
    }

    @Test
    void findByHandleReturnsEmptyForUnknown() {
        assertThat(service.findByHandle("nobody")).isEmpty();
    }

    @Test
    void articlesForFiltersAndPreservesOrder() {
        Instant t = Instant.parse("2026-04-21T08:00:00Z");
        Article a1 = new Article("Sundar speaks", "https://x/1", "google", "Sundar Pichai", "c", t);
        Article a2 = new Article("Sam interview", "https://x/2", "openai", "Sam Altman", "c", t);
        Article a3 = new Article("Generic news", "https://x/3", "techcrunch", "Random Reporter", "c", t);
        Article a4 = new Article("Sundar at I/O", "https://x/4", "google", "Sundar Pichai keynote", "c", t);

        List<Article> hits = service.articlesFor(sundar, List.of(a1, a2, a3, a4));

        assertThat(hits).containsExactly(a1, a4);
    }

    @Test
    void articlesForReturnsEmptyOnEmptyInput() {
        assertThat(service.articlesFor(sundar, List.of())).isEmpty();
        assertThat(service.articlesFor(sundar, null)).isEmpty();
    }

    @Test
    void articlesForReturnsEmptyForNullTarget() {
        Article a = new Article("t", "https://x/n", "s", "Sundar Pichai", "c", Instant.now());
        assertThat(service.articlesFor(null, List.of(a))).isEmpty();
    }
}
