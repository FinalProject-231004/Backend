package com.starta.project.domain.liveQuiz.service;

import com.starta.project.domain.liveQuiz.dto.DrawMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final RedisTemplate<String, DrawMessage> redisTemplate;
    private static final String KEY = "DrawMessages";

    public void saveDrawMessage(DrawMessage drawMessage) {
        if (!("clear".equals(drawMessage.getType()))) redisTemplate.opsForList().rightPush(KEY, drawMessage);
        else redisTemplate.delete(KEY);
    }

    public List<DrawMessage> getAllDrawMessages() {
        return redisTemplate.opsForList().range(KEY, 0, -1);
    }
}
