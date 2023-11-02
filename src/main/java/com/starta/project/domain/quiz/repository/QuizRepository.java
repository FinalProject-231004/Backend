package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizCategoryEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCategoryAndDisplayTrueOrderByCreatedAtDesc(QuizCategoryEnum category);

    List<Quiz> findAllByDisplayIsTrue(Sort id);

    List<Quiz> findAllByDisplayIsTrueAndTitleContainingOrderByIdDesc(String keyword);

    @Query ("select q from Quiz q " +
    "where q.display = false " +
    "and q.memberId = :id ")
    List<Quiz> findAllByDisplayIsFalseAndMemberId(Long id);

}
