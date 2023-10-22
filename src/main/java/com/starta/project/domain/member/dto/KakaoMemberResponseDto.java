package com.starta.project.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoMemberResponseDto {
    private Long id;
    private String nickname;
    private String profilImg;

    public KakaoMemberResponseDto(Long id, String profilImg) {
        this.id = id;
        this.profilImg = profilImg;
    }
}
