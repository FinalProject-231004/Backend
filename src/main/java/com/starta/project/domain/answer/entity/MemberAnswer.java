package com.starta.project.domain.answer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.member.entity.MemberDetail;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class MemberAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quizId;

    @Column(nullable = false)
    private boolean correct = false;

    @Column
    private Integer quizQuestionNum;

    @Column
    private Long memberId;

    @Column
    private boolean getScore =false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn( name = "member_detail_id")
    private MemberDetail memberDetail;

    public void set(boolean b) {
        this.correct = b;
    }

    public void answer(Long quizId, Long id, Integer questionNum) {
        this.quizId = quizId;
        this.memberId = id;
        this.quizQuestionNum = questionNum;
    }

    public void got(MemberDetail memberDetail) {
        this.memberDetail = memberDetail;

    }

    public void modify(boolean memberAnswer, boolean getScore) {
        this.correct = memberAnswer;
        this.getScore = getScore;
    }

    public void gainScore(boolean b) {
        this.getScore = b;
    }
    // getters, setters, etc.
}

