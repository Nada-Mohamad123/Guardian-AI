package com.example.Nudity_Detection_Service.dto;

public class DetectionResult {

    private boolean detected;
    private String category;
    private float confidence;
    private String action;
    private String contentType;

    public DetectionResult() {}

    public DetectionResult(boolean detected, String category, float confidence, String action, String contentType) {
        this.detected = detected;
        this.category = category;
        this.confidence = confidence;
        this.action = action;
        this.contentType = contentType;
    }

    public static DetectionResult safe() {
        return new DetectionResult(false, "NONE", 0f, "ALLOWED", "IMAGE");
    }

    public boolean isDetected() { return detected; }
    public void setDetected(boolean detected) { this.detected = detected; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getConfidence() { return confidence; }
    public void setConfidence(float confidence) { this.confidence = confidence; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
