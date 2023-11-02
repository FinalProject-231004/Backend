package com.starta.project.domain.quiz.service;

import com.starta.project.domain.quiz.dto.*;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoicesRepository quizChoicesRepository;
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
        list = makeMainList(quizList,list);
        return list;
    }

    //좋아요 순
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readQuizByHot() {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrue(Sort.by(Sort.Direction.DESC, "likes","id"));
        list = makeMainList(quizList,list);
        return list;
    }

    //조회순
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> readByView() {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = quizRepository.findAllByDisplayIsTrue(Sort.by(Sort.Direction.DESC, "viewCount","id"));
        list = makeMainList(quizList,list);
        return list;
    }

    //검색기능
    @Transactional(readOnly = true)
    public List<SimpleQuizDto> search(String keyword) {
        List<SimpleQuizDto> list = new ArrayList<>();
        List<Quiz> quizList = findQuizLists(keyword);
        list = makeList(quizList,list);
        return list;
    }

    //검색바 안에 제목만 보이기
    public List<TitleListsDto> searchBar(String keyword) {
        List<TitleListsDto> list = new ArrayList<>();
        List<Quiz> quizList = findQuizLists(keyword);
        for (Quiz quiz : quizList) {
            TitleListsDto titleListsDto = new TitleListsDto(quiz);
            list.add(titleListsDto);
        }
        return list;
    }

    //퀴즈 안에 있는 문제 리스트
    public List<ShowQuestionResponseDto> showQuestionList(Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 퀴즈는 없는 퀴즈입니다. "));
        List<QuizQuestion> quizQuestion = quizQuestionRepository.findAllByQuiz(quiz);
        List<ShowQuestionResponseDto> showQuestionResponseDtos = new ArrayList<>();
        for (QuizQuestion question : quizQuestion) {
            List<QuizChoices> quizChoicesList = quizChoicesRepository.findAllByQuizQuestion(question);
            List<ChoicesList> list = new ArrayList<>();
            for (QuizChoices quizChoices : quizChoicesList) {
                ChoicesList choicesList = new ChoicesList(quizChoices);
                list.add(choicesList);
            }
            // 반환
            ShowQuestionResponseDto showQuestionResponseDto = new ShowQuestionResponseDto();
            showQuestionResponseDto.set(question, list);
            showQuestionResponseDtos.add(showQuestionResponseDto);
        }
       return showQuestionResponseDtos;
    }

    //퀴즈 리스트
    private List<Quiz> findQuizLists (String keyword) {
        return quizRepository.findAllByDisplayIsTrueAndTitleContainingOrderByIdDesc(keyword);
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

    private List<SimpleQuizDto> makeMainList (List<Quiz> quizList , List<SimpleQuizDto> list) {
        for (Quiz quiz : quizList) {
            SimpleQuizDto simpleQuizDto = new SimpleQuizDto();
            simpleQuizDto.set(quiz);
            list.add(simpleQuizDto);
            if (list.size() == 8 ) break;
        }
        return list;
    }
}
