package com.starta.project.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String profilImg;

    public KakaoUserInfoDto(Long id, String profilImg) {
        this.id = id;
        this.profilImg = profilImg;
    }
}
