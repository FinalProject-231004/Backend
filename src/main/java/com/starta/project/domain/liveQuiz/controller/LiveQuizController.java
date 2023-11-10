package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.AnswerDto;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.handler.WebSocketEventListener;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final WebSocketEventListener webSocketEventListener;
    private final LiveQuizService liveQuizService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/liveChatRoom")
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        return liveQuizService.processIncomingMessage(chatMessage, messagingTemplate);
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

    @Operation(summary = "라이브퀴즈 문제만들기")
    @PostMapping("/api/quiz/liveSubmitAnswer")
    public ResponseEntity<MsgResponse> submitAnswer(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AnswerDto answerDto) {
        return ResponseEntity.status(HttpStatus.OK).body(liveQuizService.setCorrectAnswer(userDetails.getMember(), answerDto, messagingTemplate));
    }
}