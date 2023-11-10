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
    private int winnersCount; // 정답자 수
    private int mileagePoint; // 정답자에게 지급할 마일리지

    public AnswerDto(String nickName, String message, int winnersCount, int mileagePoint) {
        this.nickName = nickName;
        this.message = message;
        this.winnersCount = winnersCount;
        this.mileagePoint = mileagePoint;
    }
}
