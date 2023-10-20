package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizCategoryEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCategoryAndDisplayTrueOrderByCreatedAtDesc(QuizCategoryEnum category);

    List<Quiz> findAllByDisplayIsTrue(Sort id);

    List<Quiz> findAllByDisplayIsTrueAndTitleContainingOrderById(String keyword);

    List<Quiz> findAllByDisplayIsFalseAndMemberId(Long id);

    Optional<Quiz> findFirstByMemberId(Long id);
}
