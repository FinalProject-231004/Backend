package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final JwtUtil jwtUtil;

    // Thread-safe user storage, mapping session IDs to user information
    private final ConcurrentMap<String, String> activeUsers = new ConcurrentHashMap<>();

    // 메시지 핸들링 메소드
    @MessageMapping("/liveChatRoom")
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        return liveQuizService.sendMessage(chatMessage);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        MessageHeaders headers = event.getMessage().getHeaders();
        Message<?> connectMessage = (Message<?>) headers.get("simpConnectMessage");

        if (connectMessage != null) {
            Map<String, Object> connectHeaders = connectMessage.getHeaders();
            Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) connectHeaders.get("nativeHeaders");

            if (nativeHeaders != null && nativeHeaders.containsKey("Authorization")) {
                String tokenWithBearer = nativeHeaders.get("Authorization").get(0);
                if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
                    String token = tokenWithBearer.substring(7); // 'Bearer ' 접두사를 제거합니다.
                    Claims claims = jwtUtil.getUserInfoFromToken(token); // 토큰에서 사용자 정보를 추출하는 메소드
                    String username = claims.get("sub", String.class); // 'sub' 클레임에서 사용자 이름을 추출합니다.
                    String nickName = liveQuizService.findNickName(username);
                    System.out.println( "니쿠네임 = "+nickName);

                    // 세션 ID를 가져옵니다.
                    String sessionId = headerAccessor.getSessionId();

                    // 사용자 이름을 세션 ID와 매핑합니다.
                    activeUsers.put(sessionId, nickName);
                    // 사용자 목록을 모든 클라이언트에게 브로드캐스트합니다.
                    broadcastUserList();
                }
            }
        }
    }

    // 사용자가 연결을 끊었을 때 호출될 메소드
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (activeUsers.containsKey(sessionId)) {
            // 사용자 목록에서 제거
            activeUsers.remove(sessionId);
            // 변경된 사용자 목록을 모든 클라이언트에게 브로드캐스트
            broadcastUserList();
        }
    }

    // 접속중인 사용자 목록을 모든 클라이언트에게 브로드캐스트하는 메소드
    private void broadcastUserList() {
        // 현재 활성 사용자의 유니크한 이름 목록을 생성합니다.
        Set<String> uniqueUsernames = new HashSet<>(activeUsers.values());
        System.out.println("uniqueUsernames = " + uniqueUsernames);
        // '/topic/users'를 구독하는 클라이언트에게 유니크한 사용자 목록을 브로드캐스트합니다.
        messagingTemplate.convertAndSend("/topic/users", uniqueUsernames);
    }
}