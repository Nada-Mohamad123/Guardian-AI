package com.example.Nudity_Detection_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResult {

    private boolean detected;
    private String category;
    private float confidence;
    private String action;
    private String contentType;
    private List<BoundingBox> boundingBoxes;

    public static DetectionResult safe() {
        return new DetectionResult(false, "NONE", 0f, "ALLOWED", "IMAGE", new ArrayList<>());
    }
}