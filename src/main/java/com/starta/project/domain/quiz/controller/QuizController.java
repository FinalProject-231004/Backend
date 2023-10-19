package com.starta.project.domain.quiz.controller;

import com.starta.project.domain.quiz.dto.CreateQuizRequestDto;
import com.starta.project.domain.quiz.dto.ShowQuizResponseDto;
import com.starta.project.domain.quiz.service.QuizService;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @Operation(summary = "퀴즈 생성")
    @PostMapping("/quiz")
    public ResponseEntity<MsgDataResponse> createQuiz (@RequestPart("requestDto") CreateQuizRequestDto quizRequestDto,
                                                       @RequestPart("image") MultipartFile multipartFile,
                                                       @Parameter(hidden = true)
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails)  {
        return quizService.createQuiz(multipartFile ,quizRequestDto, userDetails.getMember() );
    }

    @Operation(summary = "퀴즈 개별 조회")
    @GetMapping("/quiz/{id}")
    public ResponseEntity<ShowQuizResponseDto> showQuiz (@PathVariable Long id,
                                                         @Parameter(hidden = true)
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return quizService.showQuiz(id, userDetails.getMember());
    }

    @Operation(summary = "퀴즈 삭제")
    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<MsgResponse> deleteQuiz(@PathVariable Long id,
                                                  @Parameter(hidden = true)
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return quizService.deleteQuiz(id,userDetails.getMember());
    }

    @Operation(summary = "좋아요 | 좋아요 취소 ")
    @PostMapping("/quiz/{id}/quizLikes")
    public ResponseEntity<MsgResponse> pushLikes (@PathVariable Long id,
                                                  @Parameter(hidden = true)
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(quizService.pushLikes(id, userDetails.getMember()));
    }

    @Operation(summary = "게시 ")
    @PutMapping("/quiz/display/{id}")
    public ResponseEntity<MsgResponse> display (@PathVariable Long id,
                                                @Parameter(hidden = true)
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return quizService.display(id, userDetails.getMember().getId());
    }

    // 수정이라 주석처리
//    @PutMapping("/quiz/{id}")
//    public ResponseEntity<MsgResponse> updateQuiz (@PathVariable Long id,
//                                                   @RequestBody CreateQuizRequestDto quizRequestDto) {
//        return ResponseEntity.ok(quizService.update(id,quizRequestDto));
//    }
}
