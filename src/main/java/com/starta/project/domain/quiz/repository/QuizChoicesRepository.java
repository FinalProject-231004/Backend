package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizChoicesRepository extends JpaRepository<QuizChoices, Long> {
    List<QuizChoices> findAllByQuizQuestion(QuizQuestion quizQuestion);

    QuizChoices findByQuizQuestionAndChecksIsTrue(QuizQuestion question);
}
