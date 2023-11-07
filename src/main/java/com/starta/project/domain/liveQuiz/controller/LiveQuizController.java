package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;

    // 클라이언트가 /app/sendMassage로 메시지를 보내면, 서버는 /topic/liveQuizChatRoom로 메시지를 보낸다.
    @MessageMapping("/liveQuizSendMassage")
    @SendTo("/api/liveQuizChatRoom")
    public ChatMessageDto sendMessage(
//            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            ChatMessageDto chatMessage) {

        return liveQuizService.sendMessage(chatMessage);

    }

    // 현재 시간을 반환하는 메소드
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

}
