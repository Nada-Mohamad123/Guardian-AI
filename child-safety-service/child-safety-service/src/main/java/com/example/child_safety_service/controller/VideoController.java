package com.example.child_safety_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.child_safety_service.service.VideoAnalysisService;  // ← ده الناقص

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoAnalysisService service;

    public VideoController(VideoAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeVideo(@RequestParam("file") MultipartFile file) {

        // 🔥 LOG هنا
        System.out.println("===== VIDEO CONTROLLER HIT =====");
        System.out.println("FILE: " + file);
        System.out.println("FILENAME: " + (file != null ? file.getOriginalFilename() : "NULL"));
        System.out.println("EMPTY?: " + (file == null || file.isEmpty()));

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Video is required");
        }

        try {
            return ResponseEntity.ok(service.analyzeVideo(file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}