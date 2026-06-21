package com.example.child_safety_service.controller;

import com.example.child_safety_service.dto.AnalysisResponse;
import com.example.child_safety_service.dto.ImageRequest;
import org.springframework.http.ResponseEntity;
import com.example.child_safety_service.service.ImageAnalysisService;  // ← ده الناقص

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageAnalysisService service;

    public ImageController(ImageAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody ImageRequest request) {

        if (request.getImageBase64() == null || request.getImageBase64().isEmpty()) {
            return ResponseEntity.badRequest().body("Image is required");
        }

        try {
            AnalysisResponse response = service.analyzeImage(request.getImageBase64());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}