package com.starta.project.domain.mypage.dto;

import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import lombok.Getter;

@Getter
public class MileageGetHistoryDto {
    private String date;
    private String description;
    private TypeEnum type;
    private Integer points;

    public MileageGetHistoryDto(MileageGetHistory mileageGetHistory) {
        this.date = mileageGetHistory.getDate().toString();
        this.description = mileageGetHistory.getDescription();
        this.type = mileageGetHistory.getType();
        this.points = mileageGetHistory.getPoints();
    }
}
