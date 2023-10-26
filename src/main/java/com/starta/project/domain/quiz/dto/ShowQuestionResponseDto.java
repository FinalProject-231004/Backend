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
    private List<ChoicesList> quizChoices;


    public void set(QuizQuestion quizQuestion, List<ChoicesList> list) {
        this.id = quizQuestion.getId();
        this.title = quizQuestion.getQuizTitle();
        this.image = quizQuestion.getImage();;
        this.quizChoices = list;
    }
}
