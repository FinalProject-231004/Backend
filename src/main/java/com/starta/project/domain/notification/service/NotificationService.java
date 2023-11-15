package com.starta.project.domain.notification.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.repository.NotificationRepository;
import com.starta.project.global.messageDto.MsgResponse;
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

    //[DB 연동]전체 알림 조회
    @Transactional(readOnly = true)
    public List<Notification> getAllNotificationByUsername(String username) {
        List<Notification> notificationList = notificationRepository.findAllByReceiverAndDeletedYn(username, 'N');
        return notificationList.stream().map(Notification::of).collect(Collectors.toList());
    }

    //[DB 연동]전체 알림 읽음 상태 업데이트
    @Transactional
    public void updateNotificationReadStatusByUsername(String username) {
        notificationRepository.bulkReadUpdate(username);
    }


    //[DB 연동]다수 알림 전송
    @Transactional
    public MsgResponse sendNotifications(Member member) {
        sseService.liveQuizSend(member);
        return new MsgResponse("라이브퀴즈 알람성공");
    }

    //[DB 연동]단일 알림 전송
    @Transactional
    public void sendNotification(Notification notification) {
        Notification notificationResult = notificationRepository.save(notification); //DB 저장
        sseService.send(notificationResult);
    }

    //[DB 연동]단일 알림 삭제
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
