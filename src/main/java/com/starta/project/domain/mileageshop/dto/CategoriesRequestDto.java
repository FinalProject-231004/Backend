package com.starta.project.domain.mileageshop.dto;

import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import lombok.Getter;

@Getter
public class CategoriesRequestDto {
    private ItemCategoryEnum category;
}
