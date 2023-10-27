package com.starta.project.domain.quiz.controller;

import com.starta.project.domain.quiz.dto.CommentCreateRequestDto;
import com.starta.project.domain.quiz.dto.CommentDeleteRequestDto;
import com.starta.project.domain.quiz.dto.CommentUpdateRequestDto;
import com.starta.project.domain.quiz.service.CommentService;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 조회 ")
    @GetMapping("/quiz/{quizId}/comments")
    public ResponseEntity<MsgDataResponse> getComments (@PathVariable Long quizId) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getComments(quizId));
    }

    @Operation(summary = "댓글 생성 ")
    @PostMapping("/quiz/comments")
    public ResponseEntity<MsgDataResponse> createComments (@RequestBody CommentCreateRequestDto requestDto,
                                                       @Parameter(hidden = true)
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.createComment(requestDto, userDetails.getMember()));
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/quiz/comments")
    public ResponseEntity<MsgDataResponse> updateComments (@RequestBody CommentUpdateRequestDto requestDto,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.status(HttpStatus.OK).body(commentService.updateComment(requestDto, userDetails.getMember()));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/quiz/comments")
    public ResponseEntity<MsgResponse> deleteComments (@RequestBody CommentDeleteRequestDto requestDto,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.deleteComment(requestDto, userDetails.getMember()));
    }
}
