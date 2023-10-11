package com.starta.project.domain.quiz.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonSerialize
public class ShowQuizResponseDto {
    private Long id;
    private String title;
    private String username;
    private String image;
    private Integer viewCount;
    private LocalDateTime createdTime;
    private String category;
    private String content;
    private List<Comment> comments;

    public void set(Quiz quiz, Integer viewcount, List<Comment> comments) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.category = quiz.getCategory();
        this.comments = comments;
        this.username = quiz.getMember().getUsername();
        this.createdTime = quiz.getCreatedAt();
        this.image = quiz.getImage();
        this.content = quiz.getContent();
        this.viewCount = viewcount;
    }

}
