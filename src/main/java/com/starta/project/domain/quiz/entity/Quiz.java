package com.starta.project.domain.quiz.entity;

import com.starta.project.domain.quiz.dto.CreateQuizRequestDto;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer complainInt= 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private Integer likes = 0;

    @Column
    private String image;

    @Enumerated(value = EnumType.STRING)
    private QuizCategoryEnum category;

    @Column
    private Boolean display = true;

    @Column
    private Long memberId;

    @Column
    private String nickname;

    public void set(CreateQuizRequestDto quizRequestDto, String image, LocalDateTime now, Long memberId, String nickname) {
        this.title = quizRequestDto.getTitle();
        this.category = quizRequestDto.getCategory();
        this.image = image;
        this.createdAt = now;
        this.content = quizRequestDto.getContent();
        this.memberId = memberId;
        this.nickname = nickname;
    }

    public void view(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public void pushLikes(Integer likesNum) {
        this.likes = likesNum;
    }

    public void play(boolean b) {
        this.display = b;
    }

//    public void update(CreateQuizRequestDto quizRequestDto) {
//        this.title = quizRequestDto.getTitle();
//        this.content = quizRequestDto.getContent();
//        this.category = quizRequestDto.getCategory();
//        this.image = quizRequestDto.getImage();
//    }
}

