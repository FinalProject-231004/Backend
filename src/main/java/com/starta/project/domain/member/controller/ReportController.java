package com.starta.project.domain.member.controller;

import com.starta.project.domain.member.service.ReportService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    @PostMapping("/quiz/{quizId}")
    public ResponseEntity<MsgResponse> reportPost(@PathVariable Long quizId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(reportService.reportPost(quizId, userDetails.getMember().getId()));
    }
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<MsgResponse> reportComment(@PathVariable Long commentId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(reportService.reportComment(commentId, userDetails.getMember().getId()));
    }
    @PostMapping("/liveChat/{chatNickname}")
    public ResponseEntity<MsgResponse> reportliveChat(@PathVariable String chatNickname,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(reportService.reportliveChat(chatNickname, userDetails.getMember().getId()));
    }
}
