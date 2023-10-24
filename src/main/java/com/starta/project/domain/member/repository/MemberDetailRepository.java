package com.starta.project.domain.member.repository;

import com.starta.project.domain.member.entity.MemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {
    Optional<MemberDetail> findByNickname(String nickname);

    MemberDetail findByMemberId(Long memberId);
}
