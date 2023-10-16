package com.starta.project.domain.notification.controller;

import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.service.NotificationService;
import com.starta.project.global.security.UserDetailsImpl;
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

    @GetMapping("/notification")
    public ResponseEntity<List<Notification>> getAllNotificationByUsername(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(userDetails.getUsername()));
    }

    @PutMapping("/notification/{username}/read")
    public ResponseEntity<List<Notification>> updateNotificationReadStatusByUsername(@PathVariable String username) {
        notificationService.updateNotificationReadStatusByUsername(username);
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(username)); //수정 후 새롭게 전달
    }

    @PutMapping("/notification/{id}/delete")
    public ResponseEntity<List<Notification>> updateNotificationDeleteStatusById(@PathVariable("id") String notificationId) {
        notificationService.updateNotificationDeleteStatusById(notificationId);
        String username = notificationService.checkUsernameByNotificationId(notificationId);
        return ResponseEntity.ok().body(notificationService.getAllNotificationByUsername(username)); //수정 후 새롭게 전달
    }
}
