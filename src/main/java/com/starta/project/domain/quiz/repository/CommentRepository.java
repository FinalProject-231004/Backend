package com.starta.project.domain.quiz.repository;

import com.starta.project.domain.quiz.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findAllByQuizId(Long id);
    List<Comment> findAllByQuizIdOrderByCreatedAtDesc(Long id);
}
