package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.service.LiveQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;

    // 클라이언트가 /api/sendMassage로 메시지를 보내면, 서버는 /topic/liveQuizChatRoom로 메시지를 보낸다.
    @MessageMapping("/liveSendMassage")
    @SendTo("/api/chat/liveChatRoom")
    public ChatMessageDto sendMessage(
//            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            ChatMessageDto chatMessage) {
        return liveQuizService.sendMessage(chatMessage);
    }



}
