package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public class AnswerDto {
    private String answer; // 정답 문구
    private Integer winnersCount; // 정답자 수
    private Integer mileagePoint; // 정답자에게 지급할 마일리지

    public AnswerDto(String answer, Integer winnersCount, Integer mileagePoint) {
        this.answer = answer;
        this.winnersCount = winnersCount;
        this.mileagePoint = mileagePoint;
    }
}
