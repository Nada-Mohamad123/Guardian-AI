package com.example.child_safety_service.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class VideoAnalysisService {

    private final WebClient pythonServiceWebClient;

    public VideoAnalysisService(WebClient pythonServiceWebClient) {
        this.pythonServiceWebClient = pythonServiceWebClient;
    }

    public Object analyzeVideo(MultipartFile file) throws Exception {

        File temp = File.createTempFile("video", ".mp4");
        try {
            file.transferTo(temp);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(temp));

            return pythonServiceWebClient.post()
                    .uri("/predict-video")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

        } finally {
            temp.delete();
        }
    }
}
