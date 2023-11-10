package com.starta.project.domain.liveQuiz.dto;

import com.starta.project.domain.member.entity.UserRoleEnum;
import lombok.Getter;

@Getter
public class LiveQuizUserInfoDto {
    UserRoleEnum role;
    String nickName;

    public LiveQuizUserInfoDto(UserRoleEnum role, String nickname) {
        this.role = role;
        this.nickName = nickname;
    }
}
