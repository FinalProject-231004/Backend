package com.starta.project.domain.liveQuiz.dto;

import com.starta.project.domain.member.entity.UserRoleEnum;
import lombok.Getter;

@Getter
public class LiveQuizUserInfoDto {
    UserRoleEnum role;
    String nickName;
    QuizUpdateDto quizUpdateDto;

    public LiveQuizUserInfoDto(UserRoleEnum role, String nickname, QuizUpdateDto quizUpdateDto){
        this.role = role;
        this.nickName = nickname;
        this.quizUpdateDto = quizUpdateDto;
    }
}
