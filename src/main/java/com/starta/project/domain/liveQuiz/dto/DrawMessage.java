package com.starta.project.domain.liveQuiz.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class DrawMessage implements Serializable {
    private String type;
    private double offsetX;
    private double offsetY;
    private String color;
    private double lineWidth;
}
