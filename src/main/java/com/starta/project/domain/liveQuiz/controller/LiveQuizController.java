package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessage;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    LiveQuizService liveQuizService;

    // 클라이언트가 /app/sendMassage로 메시지를 보내면, 서버는 /topic/liveQuizChatRoom로 메시지를 보낸다.
    @MessageMapping("/liveQuizSendMassage")
    @SendTo("/topic/liveQuizChatRoom")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        if (chatMessage != null && chatMessage.getMessage() != null) {
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
            chatMessage.setMessage(escapedMessage);
            chatMessage.setTimestamp(getCurrentTime());
        }
        return chatMessage;
    }

    // 현재 시간을 반환하는 메소드
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }

}
