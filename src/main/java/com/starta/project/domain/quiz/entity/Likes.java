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

    @ManyToOne
    @JoinColumn(name = "member_id",nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "quiz_id",nullable = false)
    private Quiz quiz;

    public void set(Quiz quiz, Member member) {
        this.quiz = quiz;
        this.member =member;
    }
    // getters, setters, etc.
}

