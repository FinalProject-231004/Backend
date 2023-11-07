package com.starta.project.domain.mypage.entity;

import com.starta.project.domain.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class AttendanceCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", nullable = false)
    private Member member;

    @Column(name = "check_datetime", nullable = false)
    private LocalDateTime checkDatetime = LocalDateTime.now();

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate = LocalDate.now();

    public AttendanceCheck(Member member) {
        this.member = member;
    }
}
