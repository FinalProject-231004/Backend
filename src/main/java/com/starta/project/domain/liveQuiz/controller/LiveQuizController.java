package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;

    @MessageMapping("/liveChatRoom")
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(
            ChatMessageDto chatMessage,
            StompHeaderAccessor accessor) {

        // StompHeaderAccessor를 통해 인증 정보에 접근
        Principal userPrincipal = accessor.getUser();

        System.out.println("principal : " + userPrincipal);
        System.out.println("principal : " + userPrincipal.getName());


        return liveQuizService.sendMessage(chatMessage);
    }

}
