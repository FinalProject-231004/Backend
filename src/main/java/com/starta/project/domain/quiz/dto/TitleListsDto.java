package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.Quiz;
import lombok.Getter;

@Getter
public class TitleListsDto {
    private Long id;
    private String title;

    public TitleListsDto(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
    }
}
