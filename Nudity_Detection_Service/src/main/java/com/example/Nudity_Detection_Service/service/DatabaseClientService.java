package com.example.Nudity_Detection_Service.service;

import com.example.Nudity_Detection_Service.dto.DetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class DatabaseClientService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseClientService.class);

    private final WebClient dbServiceWebClient;

    public DatabaseClientService(WebClient dbServiceWebClient) {
        this.dbServiceWebClient = dbServiceWebClient;
    }

    public Mono<String> createContentLog(String deviceId, String authToken, String contentType) {
        return dbServiceWebClient.post()
                .uri("/api/devices/{deviceId}/logs", deviceId)
                .header("Authorization", "Bearer " + authToken)
                .bodyValue(Map.of(
                        "contentType", contentType,
                        "sourceApp", "LiveCamera"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("logId"))
                .doOnError(e -> log.error("Failed to create content log: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<String> createDetection(String logId, String authToken, DetectionResult result) {
        return dbServiceWebClient.post()
                .uri("/api/logs/{logId}/detection", logId)
                .header("Authorization", "Bearer " + authToken)
                .bodyValue(Map.of(
                        "category", result.getCategory(),
                        "confidenceScore", result.getConfidence(),
                        "actionTaken", result.getAction()
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("detectionId"))
                .doOnError(e -> log.error("Failed to create detection: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> createAlert(String deviceId, String detectionId, String authToken,
                                  DetectionResult result) {
        return dbServiceWebClient.post()
                .uri("/api/devices/{deviceId}/auto-alert/{detectionId}", deviceId, detectionId)
                .header("Authorization", "Bearer " + authToken)
                .bodyValue(Map.of("alertType", result.getCategory() + "_DETECTED"))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to create alert: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
