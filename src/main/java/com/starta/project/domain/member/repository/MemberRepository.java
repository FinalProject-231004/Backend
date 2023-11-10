package com.starta.project.domain.member.repository;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByKakaoId(Long kakaoId);

    @Query("SELECT m.role FROM Member m JOIN m.memberDetail md WHERE md.nickname = :nickName")
    UserRoleEnum findUserRoleByNickName(@Param("nickName") String nickName);

}

