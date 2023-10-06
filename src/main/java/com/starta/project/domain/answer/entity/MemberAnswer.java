package com.starta.project.domain.answer.entity;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class MemberAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "quizChoices_id", nullable = false)
    private QuizChoices quizChoices;

    @ManyToOne
    @JoinColumn(name = "quizQuestion_id", nullable = false)
    private QuizQuestion quizQuestion;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    // getters, setters, etc.
}

