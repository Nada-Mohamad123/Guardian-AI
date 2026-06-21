package com.example.child_safety_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${detection.python-service.url}")
    private String pythonServiceUrl;

    @Value("${detection.db-service.url}")
    private String dbServiceUrl;

    @Bean
    public WebClient pythonServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(10)); // ✅ زودنا لـ 10 دقايق عشان الفيديو

        return WebClient.builder()
                .baseUrl(pythonServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(500 * 1024 * 1024)) // ✅ 500MB عشان الفيديو
                .build();
    }

    @Bean
    public WebClient dbServiceWebClient() {
        return WebClient.builder()
                .baseUrl(dbServiceUrl)
                .build();
    }
}
