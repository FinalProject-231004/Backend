package com.starta.project.domain.mileageshop.entity;

import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.mileageshop.dto.CreateMileageItemRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.net.URL;

@Entity
@Getter
@NoArgsConstructor
public class MileageShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String image;

    @Enumerated(value = EnumType.STRING)
    private ItemCategoryEnum category;

    public MileageShopItem(CreateMileageItemRequestDto requestDto, String image, ItemCategoryEnum category) {
        this.itemName = requestDto.getItemName();
        this.price = requestDto.getPrice();
        this.content = requestDto.getContent();
        this.image = image;
        this.category = category;
    }

    public void changeImage(String imageUrl) {
        this.image = imageUrl;
    }
}

