package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.quiz.entity.Likes;
import com.starta.project.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    List<Likes> findAllByQuiz(Quiz quiz);

    Optional<Likes> findByMemberIdAndQuiz(Long id, Quiz quiz);
}
