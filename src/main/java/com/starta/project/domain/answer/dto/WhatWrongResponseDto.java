package com.starta.project.domain.answer.dto;

import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import lombok.Getter;

@Getter
public class WhatWrongResponseDto {
    private Integer quizQuestionNum;
    private String quizQuestionTitle;
    private String quizChoiceAnswer;
    private boolean myAnswerIsCorrect;

    public WhatWrongResponseDto(QuizQuestion question, QuizChoices quizChoices, MemberAnswer memberAnswer) {
        this.quizQuestionNum = question.getQuestionNum();
        this.quizQuestionTitle = question.getQuizTitle();
        this.quizChoiceAnswer = quizChoices.getAnswer();
        this.myAnswerIsCorrect = memberAnswer.isCorrect();
    }
}
