package com.starta.project.domain.answer.repository;

import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.member.entity.MemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAnswerRepository extends JpaRepository<MemberAnswer,Long> {

    int countByQuizIdAndCorrectIsTrueAndMemberId(Long quizId, Long member);

    Optional<MemberAnswer> findByMemberIdAndQuizQuestionNum(Long id, Integer questionNum);

    void deleteAllByMemberDetail(MemberDetail memberDetail);
}
