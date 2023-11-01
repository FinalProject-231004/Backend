package com.starta.project.domain.quiz.dto;

import com.starta.project.domain.quiz.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long commentId;
    private String comment;
    private Long memberId;
    private String nickname;
    private String profileImage;
    private String createdAt;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.comment = comment.getComment();
        this.memberId = comment.getMemberId();
        this.nickname = comment.getNickname();
        this.profileImage = comment.getProfileImage();
        this.createdAt = comment.getCreatedAt().toString();
    }
}
