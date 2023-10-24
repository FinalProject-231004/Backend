package com.starta.project.domain.member.dto;

import lombok.Getter;

@Getter
public class MemberViewResponseDto {
    private String image;
    private String nickname;
    private String password;

    public MemberViewResponseDto(String image, String nickname) {
        this.image = image;
        this.nickname = nickname;
        this.password = "********";
    }
}
