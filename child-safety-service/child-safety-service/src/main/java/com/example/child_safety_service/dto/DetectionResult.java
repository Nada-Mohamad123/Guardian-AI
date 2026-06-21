package com.example.child_safety_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResult {

    private boolean detected;
    private String category;      // VIOLENCE, NONE
    private float confidence;
    private String action;        // BLOCKED, FLAGGED, ALLOWED
    private String contentType;   // IMAGE

    public static DetectionResult safe() {
        return new DetectionResult(false, "NONE", 0f, "ALLOWED", "IMAGE");
    }
}
