package com.starta.project.domain.mypage.repository;

import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MileageGetHistoryRepository extends JpaRepository<MileageGetHistory, Long> {
    List<MileageGetHistory> findByMemberDetailIdOrderByDateDesc(Long id);

    void deleteAllByMemberDetail(MemberDetail memberDetail);
}
