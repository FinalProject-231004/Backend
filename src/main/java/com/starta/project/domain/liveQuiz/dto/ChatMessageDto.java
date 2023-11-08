package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class ChatMessageDto {
    private Long userId;
    private String username; // 메시지 보낸 사람
    private String message; // 메시지 내용
    private String timestamp; // 메시지 보낸 시간

    public ChatMessageDto(Long userId, String username, String message, LocalDateTime timestamp) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}
