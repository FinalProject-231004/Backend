package com.starta.project.global.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starta.project.domain.member.entity.RefreshToken;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.RedisRepository;
import com.starta.project.domain.member.service.RefreshTokenService;
import com.starta.project.global.exception.Custom.CustomExpiredJwtException;
import com.starta.project.global.exception.Custom.CustomInvalidJwtException;
import com.starta.project.global.exception.Custom.CustomMalformedJwtException;
import com.starta.project.global.exception.Custom.CustomUnsupportedJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JWT 관련로그")
@Component
@RequiredArgsConstructor
public class JwtUtil {

    // JWT 데이터
    // accessToken 값, Header name, 권환 이름 (user or admin)
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    //redis 값 조회 헤더
    public static final String REFRESH_PREFIX = "refresh:";
    // 사용자 권한 값의 KEY, 권한을 구분하기 위함
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    private final long TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L; // 테스트용 2주, 밀리세컨드
//    private final long TOKEN_TIME = 5 * 60 * 1000L; // TEST용 5분, 밀리세컨드

    private final RedisRepository redisRepository;

    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey; //jwt.secret.key
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 생성자 호출 뒤에 실행, 요청의 반복 호출 방지
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    //JWT 생성
    public String createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // key 값으로 꺼내어 쓸 수 있다.
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // 토큰을 반환할 때 Header에 accessToken과 refreshToken을 담아준다:
    public void addJwtToHeader(String token, String refreshToken, HttpServletResponse res) {
        res.setHeader(AUTHORIZATION_HEADER, token);
        res.setHeader(REFRESH_HEADER, refreshToken);
    }


    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        log.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }


    // 토큰 검증, JWT 위변조 확인
    // parseBuilder() : 구성 성분을 분해하고 분석
    public boolean validateToken(String accessToken, String refreshTokenValue, HttpServletResponse res) throws IOException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
//            return accessToken;
            log.info("validateToken 1번");
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
            throw new CustomInvalidJwtException("Invalid JWT signature, 유효하지 않은 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
            throw new CustomExpiredJwtException("Expired JWT, 만료된 JWT 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
            throw new CustomUnsupportedJwtException("Unsupported JWT, 지원되지 않는 JWT 입니다.");
        } catch (Exception e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            throw new CustomMalformedJwtException("JWT claims is empty, 잘못된 JWT 입니다.");
        }
//        return false;
    }


    public String checkUsingRefreshToken(String accessToken, String refreshTokenValue, HttpServletResponse res) throws JwtException {
        String value = redisRepository.getValue(REFRESH_PREFIX + refreshTokenValue);
        if (value == null) { // refresh 만료
            log.error(REFRESH_PREFIX + refreshTokenValue);
            log.error("Expired Refresh Token, 만료된 Refresh Token 입니다.");
            throw new JwtException("만료된 Refresh Token 입니다.");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            //refreshToken 값
            RefreshToken refreshToken = objectMapper.readValue(value, RefreshToken.class);

            String username = refreshToken.getUsername();
            UserRoleEnum role = refreshToken.getRole();
            //access 토큰 다시 발급 (Bearer ~~)
            accessToken = createToken(username, role);

            //Refresh Token Rotation (기존 Refresh 토큰 제거 후 새로 발급)
            Long refreshExpireTime = refreshTokenService.getRefreshTokenTimeToLive(REFRESH_PREFIX + refreshTokenValue);
            redisRepository.setExpire(REFRESH_PREFIX + refreshTokenValue, 0L);

            String newRefreshToken = refreshTokenService.refreshTokenRotation(username, role, refreshExpireTime);

            addJwtToHeader(accessToken, newRefreshToken, res);

            return accessToken;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    // token에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getTokenFromHeader(HttpServletRequest req) {
        return req.getHeader(AUTHORIZATION_HEADER);
    }

    public String getRefreshTokenFromHeader(HttpServletRequest req) {
        return req.getHeader(REFRESH_HEADER);
    }
}