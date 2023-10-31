package com.starta.project.domain.mypage.repository;

import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MileageGetHistoryRepository extends JpaRepository<MileageGetHistory, Long> {
    List<MileageGetHistory> findByMemberDetailIdOrderByDateDesc(Long id);

    void deleteAllByMemberDetail(MemberDetail memberDetail);

    Optional<MileageGetHistory> findByDateAndMemberDetailAndType(LocalDateTime localDate, MemberDetail memberDetail, TypeEnum typeEnum);

    int countByDateAndMemberDetailAndType(LocalDateTime localDate, MemberDetail memberDetail, TypeEnum typeEnum);
}
