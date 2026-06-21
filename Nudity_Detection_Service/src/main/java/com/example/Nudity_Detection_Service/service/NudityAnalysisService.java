package com.example.Nudity_Detection_Service.service;

import com.example.Nudity_Detection_Service.dto.AnalysisResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Base64;
import java.util.Map;

@Service
public class NudityAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String ANALYZE_URL = "http://localhost:6001/analyze";
    private final String VIDEO_URL   = "http://localhost:6001/analyze-video";

    // ================= IMAGE =================
    public AnalysisResponse analyzeImage(String base64) {

        // تحويل base64 لـ raw bytes وبعتها كـ octet-stream
        byte[] imageBytes = Base64.getDecoder().decode(
                base64.contains(",") ? base64.split(",")[1] : base64
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> request = new HttpEntity<>(imageBytes, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(ANALYZE_URL, request, Map.class);

        Map body = response.getBody();
        if (body == null) {
            return new AnalysisResponse(true, "NONE", 0.0);
        }

        boolean detected  = Boolean.parseBoolean(body.get("detected").toString());
        String  category  = body.get("category").toString();
        double  confidence = Double.parseDouble(body.get("confidence").toString());

        // نحول النتيجة لـ AnalysisResponse
        // is_safe = !detected
        return new AnalysisResponse(!detected, category, confidence);
    }

    // ================= VIDEO =================
    public Object analyzeVideo(org.springframework.web.multipart.MultipartFile file) throws Exception {

        com.example.Nudity_Detection_Service.MultipartInputStreamFileResource resource =
                new com.example.Nudity_Detection_Service.MultipartInputStreamFileResource(
                        file.getInputStream(),
                        file.getOriginalFilename()
                );

        org.springframework.util.MultiValueMap<String, Object> body =
                new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        return restTemplate.postForObject(VIDEO_URL, request, Object.class);
    }
}