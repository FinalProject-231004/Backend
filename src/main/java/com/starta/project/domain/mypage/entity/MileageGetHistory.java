package com.starta.project.domain.mypage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.member.entity.MemberDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor
public class MileageGetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String description;

    @Enumerated(value = EnumType.STRING)
    private TypeEnum type;

    @Column
    private LocalDate date = LocalDate.now();

    @Column
    private Integer points;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "member_detail_id")
    private MemberDetail memberDetail;

    public MileageGetHistory(String description, TypeEnum type, Integer points, MemberDetail memberDetail) {
        this.description = description;
        this.type = type;
        this.points = points;
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
}
