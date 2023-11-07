package com.starta.project.domain.mileageshop.dto;

import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import lombok.Getter;

import java.net.URL;

@Getter
public class CreateMileageItemRequestDto {
    private String itemName;
    private Integer price;
    private String content;
    private ItemCategoryEnum category;
}
