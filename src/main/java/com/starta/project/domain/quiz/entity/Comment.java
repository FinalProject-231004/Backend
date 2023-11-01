package com.starta.project.domain.quiz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.dto.CommentCreateRequestDto;
import lombok.Getter;
import java.time.LocalDateTime;

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

    @Column
    private String profileImage;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();



    public void set(Quiz quiz, CommentCreateRequestDto requestDto, Member member) {
        this.comment = requestDto.getContent();
        this.quiz = quiz;
        this.memberId = member.getId();
        this.nickname = member.getMemberDetail().getNickname();
        this.profileImage = member.getMemberDetail().getImage();
    }

    public void update(String content) {
        this.comment = content;
    }
}

