package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.QuizChoices;
import lombok.Getter;

@Getter
public class ChoicesList {

    private Long choiceId;

    private String answer;

    public ChoicesList(QuizChoices quizChoices) {
        this.choiceId = quizChoices.getId();
        this.answer = quizChoices.getAnswer();
    }
}
