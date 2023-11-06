package com.starta.project.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private BlockStatus blockStatus;

    @Column
    private Long kakaoId;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private MemberDetail memberDetail;

    public Member(String username, String password, UserRoleEnum role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.blockStatus = BlockStatus.NOT_BLOCKED;
    }

    public Member(String username, String password, UserRoleEnum role, Long kakaoId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.blockStatus = BlockStatus.NOT_BLOCKED;
        this.kakaoId =kakaoId;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
    public void updateBlockStatus(BlockStatus newStatus) {
        this.blockStatus = newStatus;
    }
}

