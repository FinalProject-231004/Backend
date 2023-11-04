package com.starta.project.domain.answer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.quiz.entity.QuizChoices;
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
    private boolean getScore = false;

    @Column
    private String myAnswer;

    @JsonIgnore
    @ManyToOne
    @JoinColumn( name = "member_detail_id")
    private MemberDetail memberDetail;

    public void set(boolean b) {
        this.correct = b;
    }

    public void answer(Long quizId, Long id, Integer questionNum, QuizChoices quizChoices) {
        this.quizId = quizId;
        this.memberId = id;
        this.quizQuestionNum = questionNum;
        this.myAnswer = quizChoices.getAnswer();
    }

    public void got(MemberDetail memberDetail) {
        this.memberDetail = memberDetail;
    }

    public void modify(boolean memberAnswer, boolean getScore, String myAnswer) {
        this.correct = memberAnswer;
        this.getScore = getScore;
        this.myAnswer = myAnswer;
    }

    public void gainScore(boolean b) {
        this.getScore = b;
    }

    public void noMemberAnswer(Long quizId, Integer quizQuestionNum) {
        this.quizId = quizId;
        this.quizQuestionNum = quizQuestionNum;
    }
    // getters, setters, etc.
}

