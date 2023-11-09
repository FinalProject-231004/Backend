package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.handler.WebSocketEventListener;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final WebSocketEventListener webSocketEventListener;
    private final LiveQuizService liveQuizService;
    // JwtUtil과 activeUsers 맵은 이제 WebSocketEventListener에서 관리합니다.

    // 메시지 핸들링 메소드
    @MessageMapping("/liveChatRoom")
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        return liveQuizService.sendMessage(chatMessage);
    }

    // '/users.request' 메시지 매핑 메소드는 그대로 유지합니다.
    @MessageMapping("/users.request")
    public void handleUserListRequest(StompHeaderAccessor headerAccessor) {
        // WebSocketEventListener에 있는 메소드를 호출합니다.
        webSocketEventListener.handleUserListRequest(headerAccessor);
    }

    @GetMapping("/api/quiz/liveQuizUsers")
    public ResponseEntity<Set<String>> getCurrentActiveUsers() {
        Set<String> uniqueNickNames = liveQuizService.getCurrentActiveUsers(); // 변경된 부분
        return new ResponseEntity<>(uniqueNickNames, HttpStatus.OK);
    }

}