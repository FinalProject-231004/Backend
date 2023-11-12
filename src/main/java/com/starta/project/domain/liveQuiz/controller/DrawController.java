package com.starta.project.domain.liveQuiz.controller;

import com.starta.project.domain.liveQuiz.dto.DrawMessage;
import com.starta.project.domain.liveQuiz.service.DrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;
    private SimpMessagingTemplate template;

    @MessageMapping("/draw")
    @SendTo("/topic/draw")
    public DrawMessage handleDraw(DrawMessage drawMessage) {
        drawService.saveDrawMessage(drawMessage);
        return drawMessage;
    }

    @GetMapping("/api/draw/state")
    public ResponseEntity<List<DrawMessage>> getCurrentDrawState() {
        return ResponseEntity.ok(drawService.getAllDrawMessages());
    }
}
