package com.example.aidigest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.author-tracker")
public record AuthorTrackerProperties(
        boolean enabled,
        String cseApiKey,
        String cseId,
        int lookbackHours,
        List<Handle> handles
) {
    public AuthorTrackerProperties {
        if (handles == null) handles = List.of();
        if (lookbackHours <= 0) lookbackHours = 48;
    }

    public record Handle(String handle, String name, String title) {}
}
