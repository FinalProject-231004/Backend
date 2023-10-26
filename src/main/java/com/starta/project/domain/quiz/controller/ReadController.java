package com.starta.project.domain.quiz.controller;

import com.starta.project.domain.quiz.dto.CategoryDto;
import com.starta.project.domain.quiz.dto.ShowQuestionResponseDto;
import com.starta.project.domain.quiz.dto.SimpleQuizDto;
import com.starta.project.domain.quiz.dto.TitleListsDto;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.service.ReadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReadController {

    private final ReadService readService;

    @Operation(summary = "카테고리별 조회")
    @PostMapping("/quiz/category")
    public ResponseEntity<List<SimpleQuizDto>> categoryList(@RequestBody CategoryDto categoryDto ) {
        return ResponseEntity.ok(readService.readByCategory(categoryDto));
    }

    @Operation(summary = "최신 기준 조회")
    @GetMapping("/quiz")
    public ResponseEntity<List<SimpleQuizDto>> recentlyList () {
        return ResponseEntity.ok(readService.readQuiz());
    }

    @Operation(summary = "좋아요 기준 조회")
    @GetMapping("/quiz/hot")
    public ResponseEntity<List<SimpleQuizDto>> hotQuizList () {
        return ResponseEntity.ok(readService.readQuizByHot());
    }

    @Operation(summary = "죄회수 순 조회")
    @GetMapping("/quiz/viewCount")
    public ResponseEntity<List<SimpleQuizDto>> readByView () {
        return ResponseEntity.ok(readService.readByView());
    }

    @Operation(summary = "키워드 제목 검색 ")
    @GetMapping("/quiz/search")
    public ResponseEntity<List<SimpleQuizDto>> search(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(readService.search(keyword));
    }

    @Operation(summary = "퀴즈에 있는 모든 문제 리스트 ")
    @GetMapping("/quiz/quizQuestion/{id}")
    public ResponseEntity<List<ShowQuestionResponseDto>> showQuizQuestionList(@PathVariable Long id) {
        return ResponseEntity.ok(readService.showQuestionList(id));
    }

    @Operation(summary = "키워드 제목 검색 - 검색창 내부 ")
    @GetMapping("/quiz/search-bar")
    public ResponseEntity<List<TitleListsDto>> searchBar(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(readService.searchBar(keyword));
    }

}
