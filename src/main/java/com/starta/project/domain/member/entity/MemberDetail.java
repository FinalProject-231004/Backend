package com.starta.project.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.answer.entity.MemberAnswer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MemberDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mileagePoint;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private Integer complaint;

    @Column
    private String image;

    @Column(nullable = false)
    private Integer totalScore;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER ,mappedBy = "memberDetail",cascade = CascadeType.ALL)
    private List<MemberAnswer> memberAnswer = new ArrayList<>();

    public MemberDetail(String nickname) {
        this.nickname = nickname;
        this.mileagePoint = 0;
        this.complaint = 0;
        this.totalScore = 0;
    }
    public void setMember(Member member) {
        this.member = member;
    }

    public void changeMileagePoint(Integer totalPrice) {
        this.mileagePoint -= totalPrice;
    }

    public void answer(MemberAnswer memberAnswer) {
        this.memberAnswer.add(memberAnswer);
        memberAnswer.got(this);
    }
}

