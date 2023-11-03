package com.starta.project.domain.mileageshop.dto;

import lombok.Getter;

@Getter
public class OrderItemRequestDto {
    private Long itemId;
    private Integer quantity;
    private String email;
}
