package com.example.Nudity_Detection_Service.service;

import com.example.Nudity_Detection_Service.dto.DetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class DetectionClientService {

    private static final Logger log = LoggerFactory.getLogger(DetectionClientService.class);

    private final WebClient pythonServiceWebClient;

    public DetectionClientService(WebClient pythonServiceWebClient) {
        this.pythonServiceWebClient = pythonServiceWebClient;
    }

    public Mono<DetectionResult> analyzeFrame(byte[] frameBytes) {
        return pythonServiceWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(frameBytes)
                .retrieve()
                .bodyToMono(DetectionResult.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(error -> {
                    log.error("Python detection service error: {}", error.getMessage());
                    return Mono.just(DetectionResult.safe());
                });
    }
}
