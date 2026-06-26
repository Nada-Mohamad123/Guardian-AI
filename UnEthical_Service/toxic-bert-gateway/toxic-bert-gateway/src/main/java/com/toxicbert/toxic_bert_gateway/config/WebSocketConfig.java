package com.toxicbert.toxic_bert_gateway.config;

import com.toxicbert.toxic_bert_gateway.handler.AudioChunkWebSocketHandler;
import com.toxicbert.toxic_bert_gateway.handler.ToxicBertWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ToxicBertWebSocketHandler textHandler;
    private final AudioChunkWebSocketHandler audioHandler;

    public WebSocketConfig(ToxicBertWebSocketHandler textHandler,
                           AudioChunkWebSocketHandler audioHandler) {
        this.textHandler = textHandler;
        this.audioHandler = audioHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(textHandler, "/ws/detection")
                .setAllowedOrigins("*");

        registry.addHandler(audioHandler, "/ws/audio")
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*");
    }
}