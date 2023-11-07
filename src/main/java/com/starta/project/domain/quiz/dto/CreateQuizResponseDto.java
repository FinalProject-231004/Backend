package com.starta.project.domain.quiz.dto;

import lombok.Getter;

@Getter
public class CreateQuizResponseDto {
    private Long id;

    public void set(Long id) {
        this.id = id;
    }
}
