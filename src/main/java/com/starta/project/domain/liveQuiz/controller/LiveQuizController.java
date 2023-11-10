package com.starta.project.domain.liveQuiz.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.starta.project.domain.liveQuiz.dto.AnswerDto;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.handler.WebSocketEventListener;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.exception.custom.CustomRateLimiterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final WebSocketEventListener webSocketEventListener;
    private final LiveQuizService liveQuizService;
    private final SimpMessageSendingOperations messagingTemplate;
    private static final RateLimiter rateLimiter = RateLimiter.create(2);

    @MessageMapping("/liveChatRoom")
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        try {
            if (!rateLimiter.tryAcquire()) {
                liveQuizService.muteUser(chatMessage.getNickName());
                throw new CustomRateLimiterException("도배 금지!");
            }
        } catch (CustomRateLimiterException e) {
            // 사용자에게 에러 메시지를 포함한 ChatMessageDto 생성
            ChatMessageDto errorResponse = new ChatMessageDto(
                    chatMessage.getNickName(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    ChatMessageDto.MessageType.ERROR
            );

            // 에러 메시지를 해당 사용자에게만 보내기 위해 convertAndSendToUser 메서드 사용
            // 사용자의 고유 세션 ID 또는 식별자를 사용해야 함
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getNickName(), // 이 부분은 사용자를 식별할 수 있는 고유 값으로 변경해야 함
                    "/queue/errors", // 클라이언트가 구독할 에러 메시지를 받을 엔드포인트
                    errorResponse
            );
            return errorResponse;
        }

        // 정상적인 메시지 처리
        return liveQuizService.processMessage(chatMessage);
    }

    @MessageMapping("/users.request")
    public void handleUserListRequest(StompHeaderAccessor headerAccessor) {
        webSocketEventListener.handleUserListRequest(headerAccessor);
    }

    @GetMapping("/api/quiz/liveQuizUsers")
    public ResponseEntity<Set<String>> getCurrentActiveUsers() {
        Set<String> uniqueNickNames = liveQuizService.getCurrentActiveUsers(); // 변경된 부분
        return new ResponseEntity<>(uniqueNickNames, HttpStatus.OK);
    }

    @PostMapping("/api/quiz/liveSubmitAnswer")
    public void submitAnswer(AnswerDto answerDto) {
        liveQuizService.setCorrectAnswer(answerDto);
    }

}