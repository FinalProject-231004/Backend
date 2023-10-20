package com.starta.project.domain.mypage.dto;

import com.starta.project.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class MyPageMemberInfoDto {
    private String nickname;
    private String image;
    private Integer mileagePoint;

    public MyPageMemberInfoDto(Member member) {
        this.nickname = member.getMemberDetail().getNickname();
        this.image = member.getMemberDetail().getImage();
        this.mileagePoint = member.getMemberDetail().getMileagePoint();
    }
}
