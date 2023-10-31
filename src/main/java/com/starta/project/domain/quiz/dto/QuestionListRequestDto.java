package com.starta.project.domain.quiz.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class QuestionListRequestDto {
    private List<CreateQuestionRequestDto> questionList;
}
