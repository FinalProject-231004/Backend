package com.starta.project.domain.member.dto;

import lombok.Getter;

import javax.validation.constraints.Pattern;

@Getter
public class KaKaoFirstLoginDto {
    private String password;
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$",
            message = "비밀번호는 알파벳 소문자, 숫자, 특수문자를 적어도 하나씩 포함하여 8자 이상, 20자 이하로 적어주세요."
    )
    private String newPassword;

    private String newCheckPassword;
}
