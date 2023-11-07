package com.starta.project.domain.mypage.dto;

import com.starta.project.domain.mypage.entity.MileageGetHistory;
import lombok.Getter;

@Getter
public class PurchaseHistoryItemDto {
    private Long id;
    private Integer totalPrice;
    private String itemName;
    private Integer quantity;
    private String email;
    private String orderedAt;

    public PurchaseHistoryItemDto(MileageGetHistory purchaseHistory) {
        this.id = purchaseHistory.getId();
        this.totalPrice = purchaseHistory.getPoints();
        this.itemName = purchaseHistory.getDescription();
        this.quantity = purchaseHistory.getQuantity();
        this.email = purchaseHistory.getEmail();
        this.orderedAt = purchaseHistory.getDate().toString();
    }
}
