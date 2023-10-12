package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.Quiz;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SimpleQuizDto {

    private Long id;
    private Integer viewNum;
    private Integer likes;
    private String category;
    private String title;
    private String image;
    private String username;

    public void set(Quiz quiz) {
        this.id = quiz.getId();
        this.image = quiz.getImage();
        this.title = quiz.getTitle();
        this.category = quiz.getCategory();
        this.viewNum = quiz.getViewCount();
        this.likes = quiz.getViewCount();
        this.username = quiz.getMember().getUsername();
    }
}