package com.starta.project.domain.liveQuiz.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DrawingController {

    // 클라이언트로부터 그리기 데이터를 받습니다.
    @MessageMapping("/draw")
    @SendTo("/topic/draw")
    public String broadcastDraw(String drawingData) {
        // 추가적인 데이터 처리가 필요한 경우 여기서 수행
        return drawingData;
    }

    // 클라이언트로부터 캔버스 클리어 명령을 받습니다.
    @MessageMapping("/clear")
    @SendTo("/topic/clear")
    public String clearCanvasOnClients() {
        return "{\"type\":\"clear\"}";
    }
}
