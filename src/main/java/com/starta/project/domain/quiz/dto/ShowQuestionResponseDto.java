package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import lombok.Getter;

import java.util.List;

@Getter
public class ShowQuestionResponseDto {

    private Long id;
    private String title;
    private String image;
    private List<QuizChoices> quizChoices;

    public void set(QuizQuestion quizQuestion, List<QuizChoices> list) {
        this.id = quizQuestion.getId();
        this.title = quizQuestion.getQuizTitle();
        this.image = quizQuestion.getImage();;
        this.quizChoices = list;
    }
}
