package com.starta.project.domain.quiz.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.starta.project.domain.quiz.dto.CreateQuestionRequestDto;
import com.starta.project.domain.quiz.dto.QuestionListRequestDto;
import com.starta.project.domain.quiz.dto.ShowQuestionResponseDto;
import com.starta.project.domain.quiz.service.QuizQuestionService;
import com.starta.project.global.exception.Custom.CustomRateLimiterException;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;
    private static final RateLimiter rateLimiter = RateLimiter.create(0.25);

    @Operation(summary = "문제 생성 ")
    @PostMapping("/quiz/{id}/quizQuestion")
    public ResponseEntity<MsgResponse> createQuizQuestion(
            @PathVariable Long id,
            @RequestPart("image") List<MultipartFile> images, // 이미지 리스트로 변경
            @RequestPart("requestDto") List<CreateQuestionRequestDto> requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if(!rateLimiter.tryAcquire()) throw new CustomRateLimiterException("잠시만 기다려 주시면 감사하겠습니다. ");
        return quizQuestionService.createQuizQuestion(id, images, requestDto, userDetails.getMember());
    }

    @Operation(summary = "문제 개별 조회 -> 문제 번호에 따라 ")
    @GetMapping("/quiz/{id}/quizQuestion/{questionNum}")
    public ResponseEntity<ShowQuestionResponseDto> showQuizQuestion (@PathVariable Long id,
                                                                     @PathVariable Integer questionNum){
        return ResponseEntity.ok(quizQuestionService.showQuizQuestion(id, questionNum));
    }

    @Operation(summary = "문제 개별 삭제 -> 문제 번호에 따라 ")
    @DeleteMapping("/quiz/{id}/quizQuestion/{questionNum}")
    public ResponseEntity<MsgResponse> deleteQuizQuestion (@PathVariable Long id,
                                                           @PathVariable Integer questionNum,
                                                           @Parameter(hidden = true)
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return quizQuestionService.deleteQuizQuestion(id,questionNum, userDetails.getMember());
    }

    @Operation(summary = "선택지 삭제 ")
    @DeleteMapping("/quiz/quizQuestion/quizChoices/{id}")
    public ResponseEntity<MsgResponse> deleteQuizChoices (@PathVariable Long id,
                                                          @Parameter(hidden = true)
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        return quizQuestionService.deleteChoices(id, userDetails.getMember());
    }


    //수정이라 주석 처리

//    @PutMapping("/quizQuestion/{id}")
//    private ResponseEntity<MsgResponse> updateQuizQuestion (@PathVariable Long id,
//                                                            @RequestBody UpdateQuizQuestionDto updateQuizQuestionDto){
//        return ResponseEntity.ok(quizQuestionService.updateQuizQuestion(id, updateQuizQuestionDto));
//    }

}
