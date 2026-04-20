package com.example.aidigest;

import com.example.aidigest.config.AppProperties;
import com.example.aidigest.config.AuthorTrackerProperties;
import com.example.aidigest.config.TrendingAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({AppProperties.class, AuthorTrackerProperties.class, TrendingAiProperties.class})
public class AiDigestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDigestApplication.class, args);
    }
}
