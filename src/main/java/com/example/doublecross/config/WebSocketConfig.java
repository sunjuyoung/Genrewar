package com.example.doublecross.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 목적지 prefix
        config.enableSimpleBroker("/topic", "/queue");
              //  .setHeartbeatValue(new long[]{10000, 10000});  // 10초 heartbeat

        // 클라이언트가 메시지를 보낼 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");

        // 특정 사용자에게 메시지 전송 시 사용할 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/game")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS 폴백 지원

        // SockJS 없이 순수 WebSocket 엔드포인트
        registry.addEndpoint("/ws/game")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(64 * 1024);       // 64KB
        registry.setSendBufferSizeLimit(512 * 1024);   // 512KB
        registry.setSendTimeLimit(20000);               // 20초
    }
}
