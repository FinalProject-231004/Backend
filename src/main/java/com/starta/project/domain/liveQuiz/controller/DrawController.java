package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.DrawMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DrawController {

    private SimpMessagingTemplate template;

    @MessageMapping("/draw")
    @SendTo("/topic/draw")
    public DrawMessage handleDraw(DrawMessage drawMessage) {
        return drawMessage;
    }

    // 클라이언트로부터 캔버스 클리어 명령을 받습니다.
    @MessageMapping("/clear")
    @SendTo("/topic/clear")
    public String clearCanvasOnClients() {
        return "{\"type\":\"clear\"}";
    }

    @MessageMapping("/sendDrawing")
    public void receiveDrawing(String imageData) {
        // 모든 구독자에게 이미지 데이터 브로드캐스트
        template.convertAndSend("/topic/drawing", imageData);
    }
}
