package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion,Long> {

    Optional<QuizQuestion> findTopByQuizOrderByQuestionNumDesc(Quiz quiz);

    QuizQuestion findByQuizAndQuestionNum(Quiz quiz, Integer questionNum);

    List<QuizQuestion> findAllByQuiz(Quiz quiz);

    int countByQuiz(Quiz quiz);
}
