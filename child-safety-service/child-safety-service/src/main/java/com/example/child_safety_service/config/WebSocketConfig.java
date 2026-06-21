package com.example.child_safety_service.config;

import com.example.child_safety_service.handler.ViolenceFrameHandler;  // ← لازم يكون موجود
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ViolenceFrameHandler violenceFrameHandler;

    public WebSocketConfig(ViolenceFrameHandler violenceFrameHandler) {
        this.violenceFrameHandler = violenceFrameHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(violenceFrameHandler, "/ws/violence")
                .setAllowedOrigins("*");
    }
}