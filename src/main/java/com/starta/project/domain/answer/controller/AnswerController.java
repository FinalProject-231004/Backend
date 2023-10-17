package com.starta.project.domain.answer.controller;

import com.starta.project.domain.answer.service.AnswerService;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "퀴즈 응답")
    @PostMapping("/choice/{id}")
    public void choice (@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        answerService.choice(id, userDetails.getMember());
    }

    @Operation(summary = "결과 보기")
    @GetMapping("/quiz/result/{id}")
    public ResponseEntity<MsgDataResponse> result(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return answerService.result(id, userDetails.getMember());
    }
}
