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
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;

    @MessageMapping("/liveChatRoom") //app/liveSendMassage
    @SendTo("/topic/liveChatRoom")
    public ChatMessageDto sendMessage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            ChatMessageDto chatMessage) {
        return liveQuizService.sendMessage(userDetails.getMember(), chatMessage);
    }

}
