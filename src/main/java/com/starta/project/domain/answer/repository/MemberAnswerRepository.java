package com.starta.project.domain.answer.repository;

import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.member.entity.MemberDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAnswerRepository extends JpaRepository<MemberAnswer,Long> {

    void deleteAllByMemberDetail(MemberDetail memberDetail);

    Optional<MemberAnswer> findTopByMemberIdAndQuizQuestionNumAndQuizId(Long id, Integer quizQuestionNum, Long quizId);



    int countByQuizIdAndCorrectIsTrueAndMemberId(Long quizId, Long id);
}
