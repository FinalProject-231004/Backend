package com.starta.project.domain.quiz.dto;

import lombok.Getter;

@Getter
public class CommentUpdateRequestDto {
    private Long commentId;
    private String content;
}
