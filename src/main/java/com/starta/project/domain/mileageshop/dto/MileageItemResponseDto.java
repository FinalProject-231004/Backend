package com.starta.project.domain.mileageshop.dto;

import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import lombok.Getter;

import java.net.URL;

@Getter
public class MileageItemResponseDto {

    private Long id;
    private String itemName;
    private Integer price;
    private String content;
    private String image;
    private ItemCategoryEnum category;

    public MileageItemResponseDto(MileageShopItem mileageShopItem) {
        this.id = mileageShopItem.getId();
        this.itemName = mileageShopItem.getItemName();
        this.price = mileageShopItem.getPrice();
        this.content = mileageShopItem.getContent();
        this.image = mileageShopItem.getImage();
        this.category = mileageShopItem.getCategory();
    }
}
