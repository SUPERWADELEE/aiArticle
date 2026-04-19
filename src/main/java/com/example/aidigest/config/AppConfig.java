package com.example.aidigest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean("rssFetchExecutor")
    public Executor rssFetchExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
