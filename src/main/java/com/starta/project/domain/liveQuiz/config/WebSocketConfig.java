package com.starta.project.domain.liveQuiz.config;

import com.starta.project.domain.liveQuiz.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    // 메시지 브로커가 /topic으로 시작하는 메시지가 메시지를 구독하는 클라이언트에게 라우팅 되도록 정의
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 메시지 브로커에 대한 Prefix 설정
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 메시지 송신 시 prefix 설정
    }
    // topic으로 시작하는 메시지가 message-handling methods로 라우팅 되어야 한다는 것을 정의
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws").setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
