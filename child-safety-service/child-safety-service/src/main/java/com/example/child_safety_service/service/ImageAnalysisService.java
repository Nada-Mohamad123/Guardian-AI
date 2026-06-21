package com.example.child_safety_service.service;

import com.example.child_safety_service.dto.AnalysisResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ImageAnalysisService {

    private final WebClient pythonServiceWebClient;

    public ImageAnalysisService(WebClient pythonServiceWebClient) {
        this.pythonServiceWebClient = pythonServiceWebClient;
    }

    public AnalysisResponse analyzeImage(String imageBase64) {

        Map<String, String> body = Map.of("image", imageBase64);

        Map response = pythonServiceWebClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Empty response from ML service");
        }

        List<Map<String, Object>> detections =
                (List<Map<String, Object>>) response.get("detections");

        int totalBoxes =
                Integer.parseInt(response.get("total_boxes").toString());

        return new AnalysisResponse(detections, totalBoxes);
    }
}
