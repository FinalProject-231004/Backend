package com.starta.project.domain.notification.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final SseService sseService;
    private final NotificationRepository notificationRepository;


    /**
     * [DB 연동]전체 알림 조회
     */
    @Transactional(readOnly = true)
    public List<Notification> getAllNotificationByUsername(String username) {
        List<Notification> notificationList = notificationRepository.findAllByReceiver(username);
        return notificationList.stream().map(Notification::of).collect(Collectors.toList());
    }

    /**
     * [DB 연동]전체 알림 읽음 상태 업데이트
     */
    @Transactional
    public void updateNotificationReadStatusByUsername(String username) {
        notificationRepository.bulkReadUpdate(username);
    }

    /**
     * [DB 연동]단일 알림 삭제 상태 업데이트
     */
    public void updateNotificationDeleteStatusById(String notificationId) {
        notificationRepository.bulkDeletedUpdate(notificationId);
    }

    /**
     * [DB 연동]개별 알림 조회
     */
    public String checkUsernameByNotificationId(String notificationId) {
        Notification selNotification = notificationRepository.findByNotificationId(notificationId);

        String username = selNotification.getReceiver();

        return username;
    }

    /**
     * [DB 연동]다수 알림 전송
     */
    @Transactional
    public void sendNotifications(List<Notification> notificationList) {
        notificationList.forEach(notification -> {
            Notification notificationResult = notificationRepository.save(notification); //DB 저장
            sseService.send(notificationResult);
        });
    }

    /**
     * [DB 연동]단일 알림 전송
     */
    @Transactional
    public void sendNotification(Notification notification) {
        Notification notificationResult = notificationRepository.save(notification); //DB 저장
        sseService.send(notificationResult);
    }
}
