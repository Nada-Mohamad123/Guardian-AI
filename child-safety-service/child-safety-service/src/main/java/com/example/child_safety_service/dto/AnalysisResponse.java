package com.example.child_safety_service.dto;

import java.util.List;
import java.util.Map;

public class AnalysisResponse {

    private List<Map<String, Object>> detections;
    private int totalBoxes;

    public AnalysisResponse(List<Map<String, Object>> detections, int totalBoxes) {
        this.detections = detections;
        this.totalBoxes = totalBoxes;
    }

    public List<Map<String, Object>> getDetections() { return detections; }
    public void setDetections(List<Map<String, Object>> detections) { this.detections = detections; }

    public int getTotalBoxes() { return totalBoxes; }
    public void setTotalBoxes(int totalBoxes) { this.totalBoxes = totalBoxes; }
}
