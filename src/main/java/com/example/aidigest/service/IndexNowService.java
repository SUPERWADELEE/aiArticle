package com.example.aidigest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IndexNowService {

    private static final Logger log = LoggerFactory.getLogger(IndexNowService.class);
    private static final String ENDPOINT = "https://api.indexnow.org/indexnow";

    private final RestClient restClient;
    private final boolean enabled;
    private final String key;
    private final String siteUrl;

    public IndexNowService(RestClient restClient,
                           @Value("${app.indexnow.enabled:false}") boolean enabled,
                           @Value("${app.indexnow.key:}") String key,
                           @Value("${app.site-url}") String siteUrl) {
        this.restClient = restClient;
        this.enabled = enabled;
        this.key = key;
        this.siteUrl = siteUrl;
    }

    public boolean isKeyConfigured() {
        return key != null && !key.isBlank();
    }

    public String getKey() {
        return key;
    }

    @Async
    public void ping(List<String> urls) {
        if (!enabled) {
            log.debug("IndexNow disabled, skipping ping");
            return;
        }
        if (!isKeyConfigured()) {
            log.warn("IndexNow enabled but app.indexnow.key not set, skipping ping");
            return;
        }
        if (urls == null || urls.isEmpty()) {
            return;
        }

        String host;
        try {
            host = URI.create(siteUrl).getHost();
        } catch (IllegalArgumentException e) {
            log.error("Invalid app.site-url for IndexNow: {}", siteUrl);
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("host", host);
        body.put("key", key);
        body.put("keyLocation", siteUrl + "/" + key + ".txt");
        body.put("urlList", urls);

        try {
            restClient.post()
                    .uri(ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("IndexNow ping sent: host={}, urls={}", host, urls.size());
        } catch (Exception e) {
            log.warn("IndexNow ping failed: {}", e.getMessage());
        }
    }
}
