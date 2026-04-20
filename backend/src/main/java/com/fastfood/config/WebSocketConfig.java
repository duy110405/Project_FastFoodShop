package com.fastfood.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kênh để Frontend "lắng nghe" (Subscribe)
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cổng để Frontend kết nối vào WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173") // Cho phép React gọi
                .withSockJS(); // Fallback nếu trình duyệt không hỗ trợ WS thuần
    }
}