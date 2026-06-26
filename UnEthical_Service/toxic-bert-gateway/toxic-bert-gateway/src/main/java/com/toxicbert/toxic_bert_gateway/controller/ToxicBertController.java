package com.toxicbert.toxic_bert_gateway.controller;

import com.toxicbert.toxic_bert_gateway.model.*;
import com.toxicbert.toxic_bert_gateway.service.ToxicBertService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/toxicity")
public class ToxicBertController {

    private static final Logger log = LoggerFactory.getLogger(ToxicBertController.class);
    private final ToxicBertService service;

    public ToxicBertController(ToxicBertService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "spring_boot", "ok",
                "flask_service", service.checkFlaskHealth()
        ));
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResult> analyze(@Valid @RequestBody AnalyzeRequest request) {
        log.info("Received /analyze: {}",
                request.getText().substring(0, Math.min(50, request.getText().length())));
        return ResponseEntity.ok(service.analyze(
                request.getText(),
                request.getThreshold() != null ? request.getThreshold() : 0.5
        ));
    }

    @PostMapping("/analyze/batch")
    public ResponseEntity<BatchAnalyzeResult> analyzeBatch(@Valid @RequestBody BatchAnalyzeRequest request) {
        log.info("Received /analyze/batch: {} texts", request.getTexts().size());
        return ResponseEntity.ok(service.analyzeBatch(
                request.getTexts(),
                request.getThreshold() != null ? request.getThreshold() : 0.5
        ));
    }

    @PostMapping("/analyze/video")
    public ResponseEntity<VideoAnalyzeResult> analyzeVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "ar") String language,
            @RequestParam(value = "threshold", defaultValue = "0.5") double threshold) {
        log.info("Video: file={}, lang={}", file.getOriginalFilename(), language);
        VideoAnalyzeResult result = service.analyzeVideo(file, language, threshold);
        if (result == null) throw new RuntimeException("Empty response from Flask");
        return ResponseEntity.ok(result);
    }
}