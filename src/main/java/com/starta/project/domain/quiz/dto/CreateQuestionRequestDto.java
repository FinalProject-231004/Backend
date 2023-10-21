package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateQuestionRequestDto {

    private String title;
    private String content;
    private List<CreateQuizChoicesDto> quizChoices;

}