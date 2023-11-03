package com.starta.project.domain.answer.dto;

import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import lombok.Getter;

@Getter
public class WhatWrongResponseDto {
    private Integer quizQuestionNum;
    private String quizQuestionTitle;
    private String quizChoiceAnswer;

    public WhatWrongResponseDto(QuizQuestion question, QuizChoices quizChoices) {
        this.quizQuestionNum = question.getQuestionNum();
        this.quizQuestionTitle = question.getQuizTitle();
        this.quizChoiceAnswer = quizChoices.getAnswer();
    }
}
