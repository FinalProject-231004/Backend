package com.starta.project.domain.member.dto;

import lombok.Getter;

import javax.validation.constraints.Pattern;

@Getter
public class UpdateNicknameRequestDto {
    @Pattern(
            regexp = "^[가-힣a-z0-9]{2,5}$",
            message = "닉네임은 2글자 이상, 5글자 이하의 한글, 숫자, 영소문자로만 적어주세요."
    )
    private String newNickname;
}
