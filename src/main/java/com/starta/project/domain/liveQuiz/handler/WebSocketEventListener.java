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
    private final ActiveUsersManager activeUsersManager; // 의존성 주입

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

                    // 세션 ID를 가져옵니다.
                    String sessionId = headerAccessor.getSessionId();

                    // ActiveUsersManager를 사용하여 사용자 이름을 세션 ID와 매핑합니다.
                    activeUsersManager.addUser(sessionId, nickName);

                    // 사용자 목록을 모든 클라이언트에게 브로드캐스트합니다.
                    broadcastUserList();
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // ActiveUsersManager를 확인하여 세션 ID가 존재하는지 확인합니다.
        if (activeUsersManager.containsSession(sessionId)) {
            // 사용자 목록에서 제거
            activeUsersManager.removeUser(sessionId);

            // 변경된 사용자 목록을 모든 클라이언트에게 브로드캐스트
            broadcastUserList();
        }
    }

    public void handleUserListRequest(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        // ActiveUsersManager를 사용하여 현재 활성 사용자의 유니크한 이름 목록을 가져옵니다.
        Set<String> uniqueNickNames = activeUsersManager.getUniqueNickNames();

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/users",
                uniqueNickNames
        );
    }

    private void broadcastUserList() {
        // ActiveUsersManager를 사용하여 현재 활성 사용자의 유니크한 이름 목록을 가져옵니다.
        Set<String> uniqueUsernames = activeUsersManager.getUniqueNickNames();

        // '/topic/users'를 구독하는 클라이언트에게 유니크한 사용자 목록을 브로드캐스트합니다.
        messagingTemplate.convertAndSend("/topic/users", uniqueUsernames);
    }
}
