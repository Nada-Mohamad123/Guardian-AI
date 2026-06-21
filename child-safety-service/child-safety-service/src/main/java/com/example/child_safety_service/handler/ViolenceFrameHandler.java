package com.example.child_safety_service.handler;

import com.example.child_safety_service.dto.DetectionResult;
import com.example.child_safety_service.dto.DeviceSession;
import com.example.child_safety_service.service.DatabaseClientService;
import com.example.child_safety_service.service.ViolenceDetectionClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViolenceFrameHandler extends BinaryWebSocketHandler {

    private final ViolenceDetectionClientService detectionClientService;
    private final DatabaseClientService databaseClientService;
    private final ObjectMapper objectMapper;

    private final Map<String, DeviceSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        session.setBinaryMessageSizeLimit(5 * 1024 * 1024);

        String deviceId = extractParam(session, "deviceId");
        String token = extractParam(session, "token");

        if (deviceId == null || token == null) {
            log.warn("Missing deviceId or token → closing session");
            closeSilently(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.put(session.getId(), new DeviceSession(deviceId, token));

        log.info("WebSocket connected: device={}, session={}", deviceId, session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {

        DeviceSession deviceSession = sessions.get(session.getId());

        if (deviceSession == null || !session.isOpen()) {
            return;
        }

        ByteBuffer buffer = message.getPayload();

        byte[] imageBytes = new byte[buffer.remaining()];
        buffer.get(imageBytes);

        log.info("Received image bytes = {}", imageBytes.length);

        detectionClientService.analyzeFrame(imageBytes)
                .subscribe(
                        result -> handleResult(session, deviceSession, result),
                        error -> log.error("Detection error", error)
                );
    }

    private void handleResult(WebSocketSession session,
                              DeviceSession deviceSession,
                              DetectionResult result) {

        sendResponse(session, result);

        if (result.isDetected()) {
            persist(deviceSession, result);
        }
    }

    private void sendResponse(WebSocketSession session, DetectionResult result) {

        try {
            if (!session.isOpen()) return;

            String json = objectMapper.writeValueAsString(result);

            session.sendMessage(new TextMessage(json));

            log.info("Response sent to client");

        } catch (IOException e) {
            log.error("Failed to send response", e);
        }
    }

    private void persist(DeviceSession deviceSession, DetectionResult result) {

        databaseClientService.createContentLog(
                        deviceSession.getDeviceId(),
                        deviceSession.getAuthToken(),
                        result.getContentType()
                )
                .flatMap(logId -> logId == null ? Mono.empty()
                        : databaseClientService.createDetection(logId, deviceSession.getAuthToken(), result))
                .flatMap(detectionId -> detectionId == null ? Mono.empty()
                        : databaseClientService.createAlert(deviceSession.getDeviceId(), detectionId, deviceSession.getAuthToken(), result))
                .subscribe(
                        v -> log.info("Detection saved for device {}", deviceSession.getDeviceId()),
                        err -> log.error("Persist error", err)
                );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        DeviceSession removed = sessions.remove(session.getId());

        if (removed != null) {
            log.info("Connection closed: device={}, status={}",
                    removed.getDeviceId(), status);
        }
    }

    private String extractParam(WebSocketSession session, String key) {

        String query = session.getUri() != null ? session.getUri().getQuery() : null;

        if (query == null) return null;

        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }

        return null;
    }

    private void closeSilently(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ignored) {}
    }
}