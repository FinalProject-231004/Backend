package com.starta.project.domain.quiz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.dto.CreateCommentRequestDto;
import lombok.Getter;

import javax.persistence.*;


@Entity
@Getter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @Column
    private Integer complainInt = 0;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "quiz_id",nullable = false)
    private Quiz quiz;

    @Column
    private Long memberId;

    @Column
    private String nickname;

    public void set(Quiz quiz, CreateCommentRequestDto createCommentRequestDto, Member member) {
        this.comment = createCommentRequestDto.getContent();
        this.quiz = quiz;
        this.memberId = member.getId();
        this.nickname = member.getMemberDetail().getNickname();
    }

    public void update(String content) {
        this.comment = content;
    }
}

