package com.starta.project.domain.quiz.controller;

import com.starta.project.domain.quiz.dto.CreateCommentRequestDto;
import com.starta.project.domain.quiz.dto.UpdateCommentResponseDto;
import com.starta.project.domain.quiz.service.CommentService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성 ")
    @PostMapping("/comment/{id}")
    public ResponseEntity<MsgResponse> createComment (@RequestBody CreateCommentRequestDto createCommentRequestDto,
                                                      @PathVariable Long id,
                                                      @Parameter(hidden = true)
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(commentService.createComment(id,createCommentRequestDto, userDetails.getMember()));
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/comment/{id}")
    public ResponseEntity<MsgResponse> updateComment (@PathVariable Long id ,
                                                      @RequestBody UpdateCommentResponseDto updateCommentResponseDto,
                                                      @Parameter(hidden = true)
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        return commentService.updateComment(id, updateCommentResponseDto, userDetails.getMember());
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<MsgResponse> deleteComment (@PathVariable Long id,
                                                      @Parameter(hidden = true)
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteComment(id, userDetails.getMember());
    }
}
