package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;

@Getter
public class DrawMessage {
    private String type;
    private double offsetX;
    private double offsetY;
    private String color;
    private double lineWidth;
}
