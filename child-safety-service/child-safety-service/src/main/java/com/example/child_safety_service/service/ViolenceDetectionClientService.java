package com.example.child_safety_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.example.child_safety_service.dto.DetectionResult;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViolenceDetectionClientService {

    private final WebClient pythonServiceWebClient;

    /**
     * Sends a single raw image frame (bytes) to the Python YOLO service
     * and returns the detection result.
     */
    public Mono<DetectionResult> analyzeFrame(byte[] frameBytes) {
        return pythonServiceWebClient.post()
                .uri("/predict-frame")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(frameBytes)
                .retrieve()
                .bodyToMono(DetectionResult.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(error -> {
                    log.error("Python violence detection service error: {}", error.getMessage());
                    return Mono.just(DetectionResult.safe());
                });
    }
}
