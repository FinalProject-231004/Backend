package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.DrawMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DrawController {

    @MessageMapping("/draw")
    @SendTo("/topic/draw")
    public DrawMessage handleDraw(DrawMessage drawMessage) {
        // 클라이언트로부터 받은 그림 데이터를 그대로 브로드캐스트합니다.
        System.out.println(drawMessage);
        return drawMessage;
    }

    // 클라이언트로부터 캔버스 클리어 명령을 받습니다.
    @MessageMapping("/clear")
    @SendTo("/topic/clear")
    public String clearCanvasOnClients() {
        return "{\"type\":\"clear\"}";
    }
}
