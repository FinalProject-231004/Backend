package com.starta.project.domain.member.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshToken {
    private String username;
    private UserRoleEnum  role;

    public RefreshToken(String username, UserRoleEnum  role) {
        this.username = username;
        this.role = role;
    }
}
