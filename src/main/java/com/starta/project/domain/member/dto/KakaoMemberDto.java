package com.starta.project.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoMemberDto {
    private Long id;
    private String nickname;
    private String profilImg;

    public KakaoMemberDto(Long id, String profilImg) {
        this.id = id;
        this.profilImg = profilImg;
        System.out.println("KakaoMemberDto 생성완료" + id +" , " + profilImg);
    }

    @Override
    public String toString() {
        return "KakaoMemberDto{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", profilImg='" + profilImg + '\'' +
                '}';
    }
}
