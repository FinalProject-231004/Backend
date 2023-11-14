package com.starta.project.domain.notification.controller;

import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.service.NotificationService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림전체조회")
    @GetMapping("/notification")
    public ResponseEntity<List<Notification>> getAllNotificationByUsername(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(userDetails.getUsername()));
    }

    @Operation(summary = "알림전체읽음")
    @PutMapping("/notification/{username}/read")
    public ResponseEntity<List<Notification>> updateNotificationReadStatusByUsername(@PathVariable String username) {
        notificationService.updateNotificationReadStatusByUsername(username);
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(username)); //수정 후 새롭게 전달
    }

    @Operation(summary = "알림삭제")
    @DeleteMapping ("/notification/{id}/delete")
    public ResponseEntity<List<Notification>> updateNotificationDeleteStatusById(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.deleteNotification(id);
        String username = userDetails.getUsername();
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(username)); //수정 후 새롭게 전달
    }

    @Operation(summary = "라이브퀴즈알림발송")
    @GetMapping("/notification/livequiz")
    public ResponseEntity<MsgResponse> notificationLivequiz(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(notificationService.sendNotifications(userDetails.getMember()));
    }
}
