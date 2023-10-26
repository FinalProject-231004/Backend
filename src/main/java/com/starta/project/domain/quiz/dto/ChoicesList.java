package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.QuizChoices;
import lombok.Getter;

@Getter
public class ChoicesList {

    private Long id;

    private String answer;

    public ChoicesList(QuizChoices quizChoices) {
        this.id = quizChoices.getId();
        this.answer = quizChoices.getAnswer();
    }
}
