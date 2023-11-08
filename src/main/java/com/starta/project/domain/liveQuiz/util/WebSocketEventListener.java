package com.starta.project.domain.liveQuiz.util;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
//
//@Component
//@RequiredArgsConstructor
//public class WebSocketEventListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
//    private final SimpMessageSendingOperations messagingTemplate;
//    private final JwtUtil jwtUtil;
//
//    private ConcurrentMap<String, String> sessions = new ConcurrentHashMap<>();
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        MessageHeaders headers = event.getMessage().getHeaders();
//        Message<?> connectMessage = (Message<?>) headers.get("simpConnectMessage");
//
//        if (connectMessage != null) {
//            Map<String, Object> connectHeaders = connectMessage.getHeaders();
//            Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) connectHeaders.get("nativeHeaders");
//
//            if (nativeHeaders != null && nativeHeaders.containsKey("Authorization")) {
//                String tokenWithBearer = nativeHeaders.get("Authorization").get(0);
//                if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
//                    String token = tokenWithBearer.substring(7); // 'Bearer ' 접두사를 제거합니다.
//                    Claims claims = jwtUtil.getUserInfoFromToken(token);
//                    String username = claims.get("sub", String.class);
//
//                    // 사용자 이름을 세션 ID와 매핑합니다.
//                    String sessionId = (String) headers.get("simpSessionId");
//                    sessions.put(sessionId, username);
//
//                    // 현재 연결된 모든 사용자의 목록을 방송합니다.
//                    messagingTemplate.convertAndSend("/topic/presence", sessions.values());
//
//                    logger.info("User Connected: " + username);
//                }
//            }
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        // 세션 ID로 사용자를 찾아 목록에서 제거합니다.
//        String sessionId = event.getSessionId();
//        if (sessions.containsKey(sessionId)) {
//            String username = sessions.remove(sessionId);
//
//            // 변경된 접속자 목록을 방송합니다.
//            messagingTemplate.convertAndSend("/topic/presence", sessions.values());
//
//            logger.info("User Disconnected: " + username);
//        }
//    }
//}