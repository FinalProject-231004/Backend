package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ChatMessage {
    private String message; // 메시지 내용
    private String timestamp; // 메시지 보낸 시간

    public ChatMessage() {
    }

}
