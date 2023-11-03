package com.starta.project.domain.answer.repository;

import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.member.entity.MemberDetail;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberAnswerRepository extends JpaRepository<MemberAnswer,Long> {

    void deleteAllByMemberDetail(MemberDetail memberDetail);


    @Query ("SELECT a FROM MemberAnswer AS a "+
    "WHERE a.memberId = :id " +
    "AND a.quizQuestionNum =:quizQuestionNum " +
    "AND a.quizId = :quizId")
    Optional<MemberAnswer> findTopByMemberIdAndQuizQuestionNumAndQuizId(@Param("id") Long id,
                                                                        @Param("quizQuestionNum") Integer quizQuestionNum,
                                                                        @Param("quizId") Long quizId);

    int countByQuizIdAndCorrectIsTrueAndMemberId(Long quizId, Long id);

    @Query ("SELECT m FROM MemberAnswer AS m " +
    "WHERE m.memberDetail = :memberDetail " +
    "AND m.quizId = :quizId " +
    "AND m.quizQuestionNum = :questionNum")
    MemberAnswer findByMemberDetailAndQuizIdAndQuizQuestionNum(@Param("memberDetail") MemberDetail memberDetail,
                                                               @Param("quizId") Long quizId,
                                                               @Param("questionNum") Integer questionNum);
}


