package com.starta.project.domain.member.dto;

import lombok.Getter;

import javax.validation.constraints.Pattern;


@Getter
public class SignupRequestDto {

    @Pattern(
            regexp = "^[a-z][a-z0-9]{4,15}+$",
            message = "아이디는 4글자 이상, 15글자 이하이며 알파벳 소문자(a~z), 숫자로 적어주세요."
    )
    private String username;

    @Pattern(
            regexp = "^[가-힣a-z0-9]{2,5}$",
            message = "닉네임은 2글자 이상, 5글자 이하의 한글, 숫자, 영소문자로만 적어주세요."
    )
    private String nickname;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}$",
            message = "비밀번호는 알파벳 대소문자, 숫자, 특수문자를 적어도 하나씩 포함하여 8자 이상, 20자 이하로 적어주세요."
    )
    private String password;

    private boolean admin = false; // 기본값은 "USER"로 설정

}

