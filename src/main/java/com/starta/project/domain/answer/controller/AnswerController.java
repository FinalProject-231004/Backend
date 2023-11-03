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
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @Operation(summary = "문제풀이")
    @PostMapping("/quiz/choice")
    public void choice  (@RequestBody ChoiceRequestDto choiceRequestDto,
                        @Parameter(hidden = true)
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                         HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException {
        if(userDetails == null ) answerService.noMemberChoice(choiceRequestDto,httpServletRequest);
        else  answerService.choice(choiceRequestDto, userDetails.getMember());
    }

    @Operation(summary = "결과 보기")
    @GetMapping("/quiz/result/{id}")
    public ResponseEntity<MsgDataResponse> result(@PathVariable Long id,
                                                  @Parameter(hidden = true)
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException{
        if(userDetails == null) return ResponseEntity.ok(answerService.noMemberResult(id,httpServletRequest));
        return ResponseEntity.ok(answerService.result(id, userDetails.getMember()));
    }

    @Operation(summary = "문제 정답 보기")
    @GetMapping("/quiz/result-true/{quizId}")
    public ResponseEntity<MsgDataResponse> wrongQuestion (@PathVariable Long quizId,
                                                          @Parameter(hidden = true)
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(answerService.whatWrong(quizId, userDetails.getMember()));
    }
}
