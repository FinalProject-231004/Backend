package com.starta.project.domain.mypage.entity;

import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.mileageshop.dto.OrderItemRequestDto;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PurchaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mileageItem_id",nullable = false)
    private MileageShopItem mileageShopItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberDetail_id",nullable = false)
    private MemberDetail memberDetail;

    @Column(nullable = false)
    private LocalDateTime orderedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private String email;

    public PurchaseHistory(MileageShopItem mileageShopItem, MemberDetail memberDetail, OrderItemRequestDto orderItemRequestDto, Integer totalPrice) {
        this.quantity = orderItemRequestDto.getQuantity();
        this.mileageShopItem = mileageShopItem;
        this.memberDetail = memberDetail;
        this.email = orderItemRequestDto.getEmail();
        this.totalPrice = totalPrice;
    }
}

