package com.starta.project.domain.liveQuiz.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ActiveUsersManager {

    private final StringRedisTemplate redisTemplate;
    private static final String ACTIVE_USERS_KEY = "ActiveUsers";

    public void addUser(String sessionId, String nickName) {
        redisTemplate.opsForHash().put(ACTIVE_USERS_KEY, sessionId, nickName);
    }

    public void removeUser(String sessionId) {
        redisTemplate.opsForHash().delete(ACTIVE_USERS_KEY, sessionId);
    }

    public boolean containsSession(String sessionId) {
        return redisTemplate.opsForHash().hasKey(ACTIVE_USERS_KEY, sessionId);
    }

    public Set<String> getUniqueNickNames() {
        List<Object> nickNames = redisTemplate.opsForHash().values(ACTIVE_USERS_KEY);
        return new HashSet<>(nickNames.stream().map(Object::toString).collect(Collectors.toSet()));
    }
}
