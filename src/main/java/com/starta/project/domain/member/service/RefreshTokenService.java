package com.starta.project.domain.member.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starta.project.domain.member.entity.RefreshToken;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Refresh 토큰을 생성하거나 rotation하는 로직
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisRepository redisRepository;

    public static final String REFRESH_PREFIX = "refresh:";

    /**
     * refresh 토큰 저장 및 ID 반환 메소드
     *
     * @param userName 발급할 userName
     * @param role 발급할 userRole
     * @return refreshtoken key 값
     */
    public String createRefreshToken(String userName, UserRoleEnum role) {

        UUID uuid = UUID.randomUUID();
        String key = REFRESH_PREFIX + uuid;

        RefreshToken refreshToken = new RefreshToken(userName, role);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String value = objectMapper.writeValueAsString(refreshToken);
            redisRepository.save(key, value);
            redisRepository.setExpire(key, 7 * 24 * 60 * 60L);  // test 로 10분
//            redisRepository.setExpire(key, 7 * 24 * 60 * 60L);
            return uuid.toString();

        } catch (JsonProcessingException e) {
            log.error("refresh 토큰 String 변환 실패");
            throw new RuntimeException(e);
        }
    }

    public Long getRefreshTokenTimeToLive(String key) {
        return redisRepository.getTimeToLive(key);
    }

    public String refreshTokenRotation(String userName, UserRoleEnum role, Long time) {

        UUID uuid = UUID.randomUUID();
        String key = REFRESH_PREFIX + uuid;

        RefreshToken refreshToken = new RefreshToken(userName, role);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String value = objectMapper.writeValueAsString(refreshToken);
            redisRepository.save(key, value);
            redisRepository.setExpire(key, time);
            return uuid.toString();

        } catch (JsonProcessingException e) {
            log.error("refresh 토큰 String 변환 실패");
            throw new RuntimeException(e);
        }
    }
}