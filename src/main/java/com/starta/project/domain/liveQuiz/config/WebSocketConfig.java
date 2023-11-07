package com.starta.project.domain.liveQuiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // 메시지 브로커가 /topic으로 시작하는 메시지가 메시지를 구독하는 클라이언트에게 라우팅 되도록 정의
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/api/chat"); // 메시지 브로커에 대한 Prefix 설정
        config.setApplicationDestinationPrefixes("/api/send"); // 클라이언트에서 메시지 송신 시 prefix 설정
    }
    // topic으로 시작하는 메시지가 message-handling methods로 라우팅 되어야 한다는 것을 정의
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws");
    }
}
