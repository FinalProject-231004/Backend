package com.starta.project.domain.notification.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface SseRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter); //emitter 저장

    void saveEventCache(String eventCacheId, Object event); //event 저장

    Map<String, SseEmitter> findAllEmitterStartsWithUsername(String username); //specific user 관련 모든 emitter 조회

    Map<String, Object> findAllEventCacheStartsWithUsername(String username); //specific user 관련 모든 event 조회

    Map<String, SseEmitter> findAllEmitter();

    void deleteEmitterById(String id); //emitterId 기준 emitter 삭제

    void deleteAllEmitterStartsWithId(String id); //emitterId 기준 모든 emitter 삭제

    void deleteAllEventCacheStartsWithId(String id); //emitterId 기준 모든 event 삭제
}
