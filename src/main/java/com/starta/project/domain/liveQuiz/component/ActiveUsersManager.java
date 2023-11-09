package com.starta.project.domain.liveQuiz.component;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ActiveUsersManager {

    private ConcurrentMap<String, String> activeUsers = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String nickName) {
        activeUsers.put(sessionId, nickName);
    }

    public void removeUser(String sessionId) {
        activeUsers.remove(sessionId);
    }

    public boolean containsSession(String sessionId) {
        return activeUsers.containsKey(sessionId);
    }

    public Set<String> getUniqueNickNames() {
        return new HashSet<>(activeUsers.values());
    }
}
