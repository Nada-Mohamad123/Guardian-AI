package com.example.child_safety_service.handler;

import com.example.child_safety_service.dto.DetectionResult;
import com.example.child_safety_service.dto.DeviceSession;
import com.example.child_safety_service.service.DatabaseClientService;
import com.example.child_safety_service.service.ViolenceDetectionClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViolenceFrameHandler extends BinaryWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ViolenceFrameHandler.class);

    private final ViolenceDetectionClientService detectionClientService;
    private final DatabaseClientService databaseClientService;
    private final ObjectMapper objectMapper;

    private final Map<String, DeviceSession> sessions = new ConcurrentHashMap<>();

    public ViolenceFrameHandler(
            ViolenceDetectionClientService detectionClientService,
            DatabaseClientService databaseClientService,
            ObjectMapper objectMapper
    ) {
        this.detectionClientService = detectionClientService;
        this.databaseClientService = databaseClientService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setBinaryMessageSizeLimit(10 * 1024 * 1024); // 10MB

        String deviceId = extractParam(session, "deviceId");
        String token = extractParam(session, "token");

        if (deviceId == null || token == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.put(session.getId(), new DeviceSession(deviceId, token));
        log.info("Connected device={}", deviceId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {

        DeviceSession deviceSession = sessions.get(session.getId());
        if (deviceSession == null || !session.isOpen()) return;

        // ✅ الطريقة الصح لقراءة الـ ByteBuffer
        ByteBuffer buffer = message.getPayload();
        byte[] imageBytes = new byte[buffer.limit()];
        buffer.rewind(); // ارجع للأول
        buffer.get(imageBytes);

        log.info("Received frame: {} bytes", imageBytes.length);

        detectionClientService.analyzeFrame(imageBytes)
                .subscribe(
                        result -> {
                            log.info("Detection result: detected={}, confidence={}", result.isDetected(), result.getConfidence());
                            handleResult(session, deviceSession, result);
                        },
                        error -> log.error("Detection error", error)
                );
    }

    private void handleResult(WebSocketSession session, DeviceSession deviceSession, DetectionResult result) {
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
        } catch (IOException e) {
            log.error("Send failed", e);
        }
    }

    private void persist(DeviceSession deviceSession, DetectionResult result) {
        databaseClientService.createContentLog(
                        deviceSession.getDeviceId(),
                        deviceSession.getAuthToken(),
                        result.getContentType()
                )
                .flatMap(logId -> databaseClientService.createDetection(logId, deviceSession.getAuthToken(), result))
                .flatMap(detectionId -> databaseClientService.createAlert(
                        deviceSession.getDeviceId(), detectionId, deviceSession.getAuthToken(), result))
                .subscribe(
                        v -> log.info("Saved"),
                        err -> log.error("Persist error", err)
                );
    }

    private String extractParam(WebSocketSession session, String key) {
        if (session.getUri() == null) return null;
        String query = session.getUri().getQuery();
        if (query == null) return null;
        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        DeviceSession removed = sessions.remove(session.getId());
        if (removed != null) {
            log.info("Disconnected device={}", removed.getDeviceId());
        }
    }
}
