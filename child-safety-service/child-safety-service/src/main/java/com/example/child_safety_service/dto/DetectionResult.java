package com.example.child_safety_service.dto;

import java.util.List;
import java.util.Map;

public class DetectionResult {

    private boolean detected;
    private String category;
    private float confidence;
    private String contentType;
    private List<Map<String, Integer>> boundingBoxes;

    public DetectionResult() {}

    public DetectionResult(boolean detected, String category, float confidence,
                           String contentType, List<Map<String, Integer>> boundingBoxes) {
        this.detected = detected;
        this.category = category;
        this.confidence = confidence;
        this.contentType = contentType;
        this.boundingBoxes = boundingBoxes;
    }

    public static DetectionResult safe() {
        return new DetectionResult(false, "NONE", 0f, "IMAGE", List.of());
    }

    public boolean isDetected() { return detected; }
    public void setDetected(boolean detected) { this.detected = detected; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public List<Map<String, Integer>> getBoundingBoxes() { return boundingBoxes; }
    public void setBoundingBoxes(List<Map<String, Integer>> boundingBoxes) { this.boundingBoxes = boundingBoxes; }
}