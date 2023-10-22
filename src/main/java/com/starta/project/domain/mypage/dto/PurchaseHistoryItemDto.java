package com.starta.project.domain.mypage.dto;

import com.starta.project.domain.mypage.entity.PurchaseHistory;
import lombok.Getter;

@Getter
public class PurchaseHistoryItemDto {
    private Long id;
    private Integer totalPrice;
    private String itemName;
    private Integer quantity;
    private String email;
    private String orderedAt;

    public PurchaseHistoryItemDto(PurchaseHistory purchaseHistory) {
        this.id = purchaseHistory.getId();
        this.totalPrice = purchaseHistory.getTotalPrice();
        this.itemName = purchaseHistory.getMileageShopItem().getItemName();
        this.quantity = purchaseHistory.getQuantity();
        this.email = purchaseHistory.getEmail();
        this.orderedAt = purchaseHistory.getOrderedAt().toString();
    }
}
