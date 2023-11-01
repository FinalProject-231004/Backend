package com.starta.project.domain.answer.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.starta.project.domain.answer.dto.ChoiceRequestDto;
import com.starta.project.domain.answer.service.AnswerService;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "문제풀이")
    @PostMapping("/quiz/choice")
    public void choice (@RequestBody ChoiceRequestDto choiceRequestDto,
                        @Parameter(hidden = true)
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        HttpServletRequest httpServletRequest) {
        if(userDetails == null ) answerService.noMemberChoice(choiceRequestDto, httpServletRequest);
        else  answerService.choice(choiceRequestDto, userDetails.getMember());
    }

    @Operation(summary = "결과 보기")
    @GetMapping("/quiz/result/{id}")
    public ResponseEntity<MsgDataResponse> result(@PathVariable Long id,
                                                  @Parameter(hidden = true)
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  HttpServletRequest httpServletRequest) {
        if(userDetails == null) return answerService.noMemberResult(id, httpServletRequest);
        return answerService.result(id, userDetails.getMember());

    }
}
