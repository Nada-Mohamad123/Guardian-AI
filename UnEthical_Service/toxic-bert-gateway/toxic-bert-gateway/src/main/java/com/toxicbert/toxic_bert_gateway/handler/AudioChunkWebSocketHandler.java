package com.toxicbert.toxic_bert_gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toxicbert.toxic_bert_gateway.model.AudioChunkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AudioChunkWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AudioChunkWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${flask.service.url:http://localhost:5003}")
    private String flaskUrl;

    // ✅ الحد الأدنى 1 ثانية = 32000 بايت
    private static final int MIN_BYTES_TO_PROCESS = 32000;
    private static final int MAX_BUFFER_BYTES = 200_000;

    // ✅ buffer لكل session
    private final Map<String, byte[]> tempBuffer = new ConcurrentHashMap<>();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AudioChunkWebSocketHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = extractParam(session, "deviceId");
        String token = extractParam(session, "token");

        if (deviceId == null || token == null) {
            log.warn("Missing deviceId or token, closing connection");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.put(session.getId(), deviceId);
        tempBuffer.put(session.getId(), new byte[0]);
        log.info("Audio WS connected device={}", deviceId);

        Map<String, String> response = Map.of(
                "status", "connected",
                "deviceId", deviceId
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        if (!session.isOpen()) {
            log.warn("Session is closed, ignoring message");
            return;
        }

        try {
            ByteBuffer buffer = message.getPayload();
            byte[] incoming = new byte[buffer.remaining()];
            buffer.get(incoming);

            if (incoming.length == 0) return;

            // ✅ اجمع البيانات في tempBuffer
            byte[] existing = tempBuffer.getOrDefault(session.getId(), new byte[0]);
            byte[] merged = new byte[existing.length + incoming.length];
            System.arraycopy(existing, 0, merged, 0, existing.length);
            System.arraycopy(incoming, 0, merged, existing.length, incoming.length);

            log.info("Buffer size: {} bytes for session {}", merged.length, session.getId());

            // ✅ لو وصلنا 1 ثانية (32000 بايت)، ابعت لـ Flask
            if (merged.length >= MIN_BYTES_TO_PROCESS) {
                tempBuffer.put(session.getId(), new byte[0]); // reset buffer
                processAndSend(session, merged); // ← 1 ثانية كاملة
            } else if (merged.length > MAX_BUFFER_BYTES) {
                // لو البuffer كبر جداً، ابعت وامسح
                tempBuffer.put(session.getId(), new byte[0]);
                processAndSend(session, merged);
            } else {
                // لسه صغير، خزن في buffer
                tempBuffer.put(session.getId(), merged);
            }

        } catch (Exception e) {
            log.error("Error handling binary message", e);
        }
    }

    private void processAndSend(WebSocketSession session, byte[] pcmBytes) {
        try {
            // ✅ ابعت لـ Flask (الـ 1 ثانية كاملة = 32000 بايت)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            HttpEntity<byte[]> request = new HttpEntity<>(pcmBytes, headers);

            log.info("Sending {} bytes to Flask", pcmBytes.length);

            ResponseEntity<AudioChunkResult> response = restTemplate.exchange(
                    flaskUrl + "/analyze/audio-chunk",
                    HttpMethod.POST,
                    request,
                    AudioChunkResult.class
            );

            AudioChunkResult result = response.getBody();
            if (result != null && session.isOpen()) {
                String jsonResponse = objectMapper.writeValueAsString(result);
                session.sendMessage(new TextMessage(jsonResponse));
            }

        } catch (Exception e) {
            log.error("Error processing audio", e);
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(
                            objectMapper.writeValueAsString(Map.of("error", e.getMessage()))
                    ));
                }
            } catch (Exception ignore) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // ✅ لو في بيانات متبقية في البuffer، ابعتها
        byte[] remaining = tempBuffer.remove(session.getId());
        if (remaining != null && remaining.length >= 3200) { // على الأقل 0.1 ثانية
            processAndSend(session, remaining);
        }
        sessions.remove(session.getId());
        log.info("Audio WS disconnected device={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error for session {}", session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private String extractParam(WebSocketSession session, String key) {
        if (session.getUri() == null) return null;
        String query = session.getUri().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }
}