package com.starta.project.domain.liveQuiz.handler;

import com.starta.project.domain.liveQuiz.component.ActiveUsersManager;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final LiveQuizService liveQuizService;
    private final JwtUtil jwtUtil;
    private final ActiveUsersManager activeUsersManager;

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
                    String token = tokenWithBearer.substring(7);
                    Claims claims = jwtUtil.getUserInfoFromToken(token);
                    String username = claims.get("sub", String.class);
                    String nickName = liveQuizService.findNickName(username);
                    String sessionId = headerAccessor.getSessionId();
                    activeUsersManager.addUser(sessionId, nickName);
                    broadcastUserList();
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (activeUsersManager.containsSession(sessionId)) {
            activeUsersManager.removeUser(sessionId);
            broadcastUserList();
        }
    }

    public void handleUserListRequest(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Set<String> uniqueNickNames = activeUsersManager.getUniqueNickNames();
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/users",
                uniqueNickNames
        );
    }

    private void broadcastUserList() {
        Set<String> uniqueUsernames = activeUsersManager.getUniqueNickNames();
        messagingTemplate.convertAndSend("/topic/users", uniqueUsernames);
    }
}
