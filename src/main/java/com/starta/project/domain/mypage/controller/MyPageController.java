package com.starta.project.domain.mypage.controller;

import com.starta.project.domain.mypage.service.MyPageService;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마일리지 구매내역")
    @GetMapping("/mypage/purchase-history")
    public ResponseEntity<MsgDataResponse> getItemsByCategory(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(myPageService.getPurchaseHistory(userDetails.getMember()));
    }

    @Operation(summary = "임시 게시글 조회")
    @GetMapping("/mypage/un-display")
    public ResponseEntity<List<Quiz>> showUnDisplayQuiz(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(myPageService.showUnDisplayQuiz(userDetails.getMember()));
    }
}
