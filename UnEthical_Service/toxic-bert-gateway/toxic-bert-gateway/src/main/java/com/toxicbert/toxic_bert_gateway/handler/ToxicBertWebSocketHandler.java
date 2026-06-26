package com.toxicbert.toxic_bert_gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toxicbert.toxic_bert_gateway.model.AnalyzeResult;
import com.toxicbert.toxic_bert_gateway.service.ToxicBertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToxicBertWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ToxicBertWebSocketHandler.class);

    private final ToxicBertService service;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public ToxicBertWebSocketHandler(ToxicBertService service) {
        this.service = service;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = extractParam(session, "deviceId");
        String token    = extractParam(session, "token");

        if (deviceId == null || token == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.put(session.getId(), deviceId);
        log.info("WS connected device={}", deviceId);

        session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(Map.of(
                        "status", "connected",
                        "deviceId", deviceId
                ))
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!session.isOpen()) return;

        Map<String, Object> body = objectMapper.readValue(message.getPayload(), Map.class);
        String text      = (String) body.getOrDefault("text", "");
        double threshold = body.containsKey("threshold")
                ? ((Number) body.get("threshold")).doubleValue()
                : 0.5;

        if (text.isBlank()) {
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("error", "empty text"))
            ));
            return;
        }

        try {
            AnalyzeResult result = service.analyze(text, threshold);
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(result)
            ));
        } catch (Exception e) {
            log.error("WS analyze error", e);
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(Map.of("error", e.getMessage()))
            ));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String deviceId = sessions.remove(session.getId());
        log.info("WS disconnected device={}", deviceId);
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
}