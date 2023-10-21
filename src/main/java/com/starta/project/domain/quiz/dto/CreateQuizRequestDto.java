package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.QuizCategoryEnum;
import lombok.Getter;

@Getter
public class CreateQuizRequestDto {
    private String title;
    private QuizCategoryEnum category;
    private String content;

}
