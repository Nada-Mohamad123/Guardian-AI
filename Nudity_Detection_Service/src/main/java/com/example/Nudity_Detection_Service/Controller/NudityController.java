package com.example.Nudity_Detection_Service.Controller;


import com.example.Nudity_Detection_Service.dto.AnalysisResponse;
import com.example.Nudity_Detection_Service.dto.ImageRequest;
import com.example.Nudity_Detection_Service.service.NudityAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/nudity")
public class NudityController {

    private final NudityAnalysisService service;

    public NudityController(NudityAnalysisService service) {
        this.service = service;
    }

    // ================= IMAGE =================
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestBody ImageRequest request) {

        if (request.getImageBase64() == null || request.getImageBase64().isEmpty()) {
            return ResponseEntity.badRequest().body("Image is required");
        }

        try {
            AnalysisResponse response =
                    service.analyzeImage(request.getImageBase64());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= VIDEO =================
    @PostMapping("/analyze-video")
    public ResponseEntity<?> analyzeVideo(@RequestParam("file") MultipartFile file) {

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
