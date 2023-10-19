package com.starta.project.domain.quiz.service;

import com.starta.project.domain.quiz.dto.CategoryDto;
import com.starta.project.domain.quiz.dto.SimpleQuizDto;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ReadService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    // 카테고리 별 정렬
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readByCategory(CategoryDto categoryDto) {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByCategoryAndDisplayTrueOrderByCreatedAtDesc(categoryDto.getCategory());
        list = makeList(quizList,list);

        return list;
    }

    // 최신순
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readQuiz() {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrue(Sort.by(Sort.Direction.DESC,"id"));
        list = makeList(quizList,list);
        return list;
    }

    //좋아요 순
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readQuizByHot() {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrue(Sort.by(Sort.Direction.DESC, "likes","id"));
        list = makeList(quizList,list);
        return list;
    }

    //조회순
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readByView() {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrue(Sort.by(Sort.Direction.DESC, "viewCount","id"));
        list = makeList(quizList,list);
        return list;
    }

    @Transactional(readOnly = true)
    public List<SimpleQuizDto> search(String keyword) {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrueAndTitleContainingOrderById(keyword);
        list = makeList(quizList,list);
        return list;
    }

    public List<QuizQuestion> showQuestionList(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 퀴즈는 없는 퀴즈입니다. "));
        List<QuizQuestion> quizQuestion = quizQuestionRepository.findAllByQuiz(quiz);
       return quizQuestion;
    }

    //리스트 만들기
    private List<SimpleQuizDto> makeList (List<Quiz> quizList , List<SimpleQuizDto> list) {
        for (Quiz quiz : quizList) {
            SimpleQuizDto simpleQuizDto = new SimpleQuizDto();
            simpleQuizDto.set(quiz);
            list.add(simpleQuizDto);
        }
        return list;
    }
}
