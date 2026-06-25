package com.toxicbert.toxic_bert_gateway.service;

import com.toxicbert.toxic_bert_gateway.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ToxicBertService {

    private static final Logger log = LoggerFactory.getLogger(ToxicBertService.class);

    private final RestTemplate restTemplate;

    @Value("${flask.service.url:http://localhost:5003}")
    private String flaskUrl;

    public ToxicBertService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AnalyzeResult analyze(String text, double threshold) {
        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("threshold", threshold);
        try {
            ResponseEntity<AnalyzeResult> response =
                    restTemplate.postForEntity(flaskUrl + "/analyze", buildRequest(body), AnalyzeResult.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Analyze failed", e);
            throw new RuntimeException("Flask analyze failed: " + e.getMessage());
        }
    }

    public BatchAnalyzeResult analyzeBatch(List<String> texts, double threshold) {
        Map<String, Object> body = new HashMap<>();
        body.put("texts", texts);
        body.put("threshold", threshold);
        try {
            ResponseEntity<BatchAnalyzeResult> response =
                    restTemplate.postForEntity(flaskUrl + "/analyze/batch", buildRequest(body), BatchAnalyzeResult.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Batch analyze failed", e);
            throw new RuntimeException("Flask batch failed: " + e.getMessage());
        }
    }

    public VideoAnalyzeResult analyzeVideo(MultipartFile file, String language, double threshold) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("file", new MultipartInputStreamFileResource(
                    file.getInputStream(), file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException("File read error");
        }
        body.add("language", language);
        body.add("threshold", String.valueOf(threshold));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        try {
            ResponseEntity<VideoAnalyzeResult> response =
                    restTemplate.postForEntity(flaskUrl + "/analyze/video",
                            new HttpEntity<>(body, headers), VideoAnalyzeResult.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Video analyze failed", e);
            throw new RuntimeException("Flask video failed: " + e.getMessage());
        }
    }

    public Map<?, ?> checkFlaskHealth() {
        try {
            return restTemplate.getForObject(flaskUrl + "/health", Map.class);
        } catch (Exception e) {
            return Map.of("status", "unreachable");
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}

class MultipartInputStreamFileResource extends InputStreamResource {
    private final String filename;

    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    @Override public String getFilename() { return filename; }
    @Override public long contentLength() { return -1; }
}