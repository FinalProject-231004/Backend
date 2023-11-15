package com.starta.project.domain.notification.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.entity.NotificationType;
import com.starta.project.domain.notification.repository.SseRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; //60분

    private final SseRepositoryImpl sseRepository;

    //[SSE 통신]연결
    public SseEmitter subscribe(String userName, String lastEventId) {
        String emitterId = userName + "_" + System.currentTimeMillis();

        SseEmitter sseEmitter = sseRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        /* 상황별 emitter 삭제 처리 */
        sseEmitter.onCompletion(() -> sseRepository.deleteEmitterById(emitterId)); //완료 시, 타임아웃 시, 에러 발생 시
        sseEmitter.onTimeout(() -> sseRepository.deleteEmitterById(emitterId));
        sseEmitter.onError((e) -> sseRepository.deleteEmitterById(emitterId));

        /* 503 Service Unavailable 방지용 dummy event 전송 */
        send(sseEmitter, emitterId, createDummyNotification(userName));

        /* client가 미수신한 event 목록이 존재하는 경우 */
        if(!lastEventId.isEmpty()) { //client가 미수신한 event가 존재하는 경우 이를 전송하여 유실 예방
            Map<String, Object> eventCaches = sseRepository.findAllEventCacheStartsWithUsername(userName); //id에 해당하는 eventCache 조회
            eventCaches.entrySet().stream() //미수신 상태인 event 목록 전송
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> emitEventToClient(sseEmitter, entry.getKey(), entry.getValue()));
        }

        return sseEmitter;
    }

    //[SSE 통신]specific user에게 알림 전송
    public void send(Notification notificationResult) {
        Notification notification = createNotification(notificationResult);
        /* 로그인한 client의 sseEmitter 전체 호출 */
        Map<String, SseEmitter> sseEmitters = sseRepository.findAllEmitterStartsWithUsername(notificationResult.getReceiver());
        sseEmitters.forEach(
                (key, sseEmitter) -> {
                    sseRepository.saveEventCache(key, notification); //저장
                    emitEventToClient(sseEmitter, key, notification); //전송
                }
        );
    }

    //[SSE 통신]dummy data 생성 : 503 Service Unavailable 방지
    private Notification createDummyNotification(String receiver) {
        return Notification.builder()
                .notificationId(receiver + "_" + System.currentTimeMillis())
                .receiver(receiver)
                .content("send dummy data to client.")
                .notificationType(NotificationType.NOTICE.getAlias())
                .url(NotificationType.NOTICE.getPath())
                .readYn('N')
                .deletedYn('N')
                .build();
    }

    //[SSE 통신]라이브퀴즈 알림 data 생성
    private Notification createLiveQuizNotification(String receiver) {
        String sseReceiver = StringUtils.substringBefore(receiver, "_");
        return Notification.builder()
                .notificationId(receiver)
                .receiver(sseReceiver)
                .content("send live quiz data to client.")
                .notificationType(NotificationType.LIVEQUIZ.getAlias())
                .url(NotificationType.LIVEQUIZ.getPath())
                .readYn('N')
                .deletedYn('N')
                .created_at(LocalDateTime.now())
                .build();
    }

    //[SSE 통신]notification type별 data 생성
    private Notification createNotification(Notification notificationResult) {
        if(notificationResult.getNotificationType().equals(NotificationType.COMMENT.getAlias())) { //댓글
            return Notification.builder()
                    .id(notificationResult.getId())
                    .notificationId(notificationResult.getNotificationId())
                    .receiver(notificationResult.getReceiver())
                    .content(notificationResult.getContent())
                    .notificationType(NotificationType.COMMENT.getAlias())
                    .url(notificationResult.getUrl())
                    .readYn('N')
                    .deletedYn('N')
                    .created_at(LocalDateTime.now())
                    .build();
        } else if(notificationResult.getNotificationType().equals(NotificationType.LIKEQUIZ.getAlias())) { //좋아요
            return Notification.builder()
                    .id(notificationResult.getId())
                    .notificationId(notificationResult.getNotificationId())
                    .receiver(notificationResult.getReceiver())
                    .content(notificationResult.getContent())
                    .notificationType(NotificationType.LIKEQUIZ.getAlias())
                    .url(notificationResult.getUrl())
                    .readYn('N')
                    .deletedYn('N')
                    .created_at(LocalDateTime.now())
                    .build();
        } else {
            return null;
        }
    }

    //[SSE 통신]notification type별 event 전송
    private void send(SseEmitter sseEmitter, String emitterId, Object data) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name("sse")
                    .data(data, MediaType.APPLICATION_JSON));
        } catch(IOException exception) {
            sseRepository.deleteEmitterById(emitterId);
            sseEmitter.completeWithError(exception);
        }
    }

    //[SSE 통신]
    private void emitEventToClient(SseEmitter sseEmitter, String emitterId, Object data) {
        try {
            send(sseEmitter, emitterId, data);
        } catch (Exception e) {
            sseRepository.deleteEmitterById(emitterId);
            throw new RuntimeException("Connection Failed.");
        }
    }

    //[SSE 통신] 라이브 퀴즈 알림
    public void liveQuizSend(Member member) {
        String role = String.valueOf(member.getRole());
        if(role.equals("ADMIN")){
            Map<String, SseEmitter> sseEmitters = sseRepository.findAllEmitter();
            sseEmitters.forEach(
                    (key, sseEmitter) -> {
                        Notification notification = createLiveQuizNotification(key);
                        sseRepository.saveEventCache(key, notification); //저장
                        emitEventToClient(sseEmitter, key, notification); //전송
                    }
            );
        }
    }
}
