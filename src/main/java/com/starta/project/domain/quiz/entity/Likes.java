package com.starta.project.domain.quiz.entity;

import com.starta.project.domain.member.entity.Member;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "quiz_id",nullable = false)
    private Quiz quiz;

    public void set(Quiz quiz, Member member) {
        this.quiz = quiz;
        this.memberId = member.getId();
    }
    // getters, setters, etc.
}

