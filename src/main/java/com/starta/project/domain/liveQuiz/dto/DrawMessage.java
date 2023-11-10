package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;

@Getter
public class DrawMessage {
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String color;
    private double lineWidth;
}
