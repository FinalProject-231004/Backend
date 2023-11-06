package com.starta.project.domain.mypage.repository;

import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MileageGetHistoryRepository extends JpaRepository<MileageGetHistory, Long> {
    List<MileageGetHistory> findByMemberDetailIdOrderByDateDesc(Long id);

    void deleteAllByMemberDetail(MemberDetail memberDetail);

    @Query("SELECT COUNT(m) FROM MileageGetHistory m " +
            "WHERE DATE_FORMAT(m.date, '%Y-%m-%d') = DATE_FORMAT(:localDate, '%Y-%m-%d')" +
            "AND m.memberDetail = :memberDetail " +
            "AND m.type = :typeEnum " )
    int countByDateAndMemberDetailAndType(@Param("localDate") LocalDateTime localDate,
                                          @Param("memberDetail") MemberDetail memberDetail,
                                          @Param("typeEnum") TypeEnum typeEnum);

    @Query ("SELECT m FROM MileageGetHistory m " +
            "WHERE DATE_FORMAT(m.date,'%Y-%m-%d') = DATE_FORMAT(:localDate, '%Y-%m-%d')" +
            "AND m.memberDetail = :memberDetail " +
            "AND m.type = :typeEnum" )
    Optional<MileageGetHistory> findFirstByDateAndMemberDetailAndType(LocalDateTime localDate, MemberDetail memberDetail, TypeEnum typeEnum);

    @Query ("SELECT m FROM MileageGetHistory m " +
            "WHERE m.memberDetail = :memberDetail " +
            "AND m.type = :spend " +
            "ORDER BY m.date DESC ")
    List<MileageGetHistory> findAllByMemberDetailAndTypeOrderByDateDesc(MemberDetail memberDetail, TypeEnum spend);
}
