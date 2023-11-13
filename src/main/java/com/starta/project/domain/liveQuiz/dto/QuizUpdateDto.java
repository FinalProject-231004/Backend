package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

@Getter
public class QuizUpdateDto implements Serializable {
    private Set<String> correctAnsweredUsers;
    private Integer remainingWinners;
    private Integer answerLength;
    private Integer mileagePoint;

    public QuizUpdateDto(Set<String> correctAnsweredUsers, Integer remainingWinners, Integer answerLength, Integer mileagePoint) {
        this.correctAnsweredUsers = correctAnsweredUsers;
        this.remainingWinners = remainingWinners;
        this.answerLength = answerLength;
        this.mileagePoint = mileagePoint;
    }
}
