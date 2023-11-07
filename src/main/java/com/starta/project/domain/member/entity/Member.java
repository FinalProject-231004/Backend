package com.starta.project.domain.member.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity 
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean block;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @Column
    private Long kakaoId;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private MemberDetail memberDetail;

    public Member(String username, String password, UserRoleEnum role) {
        this.username = username;
        this.password = password;
        this.block = false;
        this.role = role;
    }

    public Member(String username, String password, UserRoleEnum role, Long kakaoId) {
        this.username = username;
        this.password = password;
        this.block = false;
        this.role = role;
        this.kakaoId = kakaoId;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public void setRole(UserRoleEnum role) {
        this.role = role;
    }

}

