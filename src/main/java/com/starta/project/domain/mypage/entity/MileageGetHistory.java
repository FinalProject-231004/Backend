package com.starta.project.domain.mypage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "mileageHistory")
public class MileageGetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String description;

    @Enumerated(value = EnumType.STRING)
    private TypeEnum type;

    @Column
    private LocalDateTime date = LocalDateTime.now();

    @Column
    private Integer points;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_detail_id")
    private MemberDetail memberDetail;

    @Column
    private String email;

    @Column
    private Integer quantity;

    public MileageGetHistory(String description, TypeEnum type, Integer points, MemberDetail memberDetail) {
        this.description = description;
        this.type = type;
        this.points = points;
        this.memberDetail = memberDetail;
    }

    public MileageGetHistory(MileageShopItem mileageShopItem, MemberDetail memberDetail, OrderItemRequestDto orderItemRequestDto, Integer totalPrice, TypeEnum type) {
        this.description = mileageShopItem.getItemName();
        this.type = type;
        this.points = totalPrice;
        this.email = orderItemRequestDto.getEmail();
        this.quantity = orderItemRequestDto.getQuantity();
        this.memberDetail = memberDetail;
    }

    public void getFromQuiz(MemberDetail memberDetail, Integer i, String des) {
        this.description = des;
        this.points = i;
        this.memberDetail = memberDetail;
        this.type = TypeEnum.QUIZ_CREATE;
    }

    public void getFromAnswer(Integer i, String des, MemberDetail memberDetail) {
        this.description = des;
        this.points = i;
        this.memberDetail = memberDetail;
        this.type = TypeEnum.QUIZ_SOLVE;
    }

    public void spendMileage(String des, int i, MemberDetail memberDetail) {
        this.description = des;
        this.points = -i;
        this.memberDetail = memberDetail;
        this.type = TypeEnum.SPEND;
    }
}
