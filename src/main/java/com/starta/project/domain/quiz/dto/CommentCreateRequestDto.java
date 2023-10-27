package com.starta.project.domain.quiz.dto;

import lombok.Getter;

@Getter
public class CommentCreateRequestDto {
    private Long quizId;
    private String content;
}
