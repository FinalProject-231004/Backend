package com.starta.project.domain.mypage.repository;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.mypage.entity.AttendanceCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {
    Optional<AttendanceCheck> findByMemberAndCheckDate(Member member, LocalDate today);

    void deleteAllByMember(Member member);
}