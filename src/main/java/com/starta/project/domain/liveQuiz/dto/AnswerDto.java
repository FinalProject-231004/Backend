package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerDto {

        private String nickName; // 메시지 보낸 사람
        private String message; // 메시지 내용

        public AnswerDto(String nickName, String message) {
            this.nickName = nickName;
            this.message = message;
        }
}
