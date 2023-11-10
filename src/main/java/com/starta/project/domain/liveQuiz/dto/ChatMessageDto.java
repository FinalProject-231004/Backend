package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {

    private String nickName; // 메시지 보낸 사람
    private String message; // 메시지 내용
    private String timestamp; // 메시지 보낸 시간
    private String type;

    public enum MessageType {
        ERROR,
        NOTIFICATION
    }

    public ChatMessageDto(String nickName, String message, LocalDateTime timestamp) {
        this.nickName = nickName;
        this.message = message;
        this.timestamp = timestamp.toString();
    }
    public ChatMessageDto(String nickName, String message, LocalDateTime timestamp, MessageType type) {
        this.nickName = nickName;
        this.message = message;
        this.timestamp = timestamp.toString();
        this.type = type.toString();
    }

}
