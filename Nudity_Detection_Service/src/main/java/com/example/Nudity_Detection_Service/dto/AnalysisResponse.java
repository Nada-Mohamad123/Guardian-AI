package com.example.Nudity_Detection_Service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisResponse {

    @JsonProperty("is_safe")
    private boolean isSafe;
    private String reason;
    private double confidence;

    public AnalysisResponse() {
    }

    public AnalysisResponse(boolean isSafe, String reason, double confidence) {
        this.isSafe = isSafe;
        this.reason = reason;
        this.confidence = confidence;
    }

    public boolean isSafe() {
        return isSafe;
    }
    @JsonIgnore
    public void setSafe(boolean safe) {
        isSafe = safe;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
