package com.starta.project.global.jwt;


import com.starta.project.global.exception.Custom.CustomExpiredJwtException;
import com.starta.project.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT 필터 시작");
        String accessTokenValue = jwtUtil.getTokenFromHeader(req);
        String refreshTokenValue = jwtUtil.getRefreshTokenFromHeader(req);
        String requestURI = req.getRequestURI();

        // 엑세스토큰 만료시 리프레시토큰 검증 및 엑세스토큰 재발급요청 로직
        if ("/api/token/reissue".equals(requestURI) && refreshTokenValue != null) {
            try {
                jwtUtil.checkUsingRefreshToken(accessTokenValue, refreshTokenValue, res); // 리프레시 토큰 검증 및 새로운 액세스 토큰 발급
                return; // 필터 체인 종료
            } catch (JwtException jwtEx) {
                log.error("Refresh token expired or invalid.", jwtEx);
                res.setContentType("application/json");
                res.setCharacterEncoding("utf-8");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"message\":\"Expired Refresh Token. 토큰이 만료되었습니다.\"}");
                return; // 필터 체인 종료
            }
        }


        // 로그인 또는 재발급 요청이 아닌 경우
        if (!(requestURI.equals("/") || requestURI.equals("/api/member/login") || requestURI.equals("/api/member/signup"))) {
            if (StringUtils.hasText(accessTokenValue)) {
                // JWT 토큰 substring
                accessTokenValue = jwtUtil.substringToken(accessTokenValue);
                log.info("validateToken 시작");

                //access 토큰이 유효하면 그대로 반환, 만료되어 refresh토큰 통해 반환되면 새로운 토큰 발급
                try {
                    if (jwtUtil.validateToken(accessTokenValue, refreshTokenValue, res)) {
                        Claims info = jwtUtil.getUserInfoFromToken(accessTokenValue);
                        log.info("Claims info" + info);
                        setAuthentication(info.getSubject());
                        log.info("setAuthentication");
                    }
                } catch (CustomExpiredJwtException e) {  // 액세스 토큰 만료 발생 시
                    res.setContentType("application/json");
                    res.setCharacterEncoding("utf-8");
                    res.setStatus(401);
                    res.getWriter().write("{\"msg\":\"Expired Access Token. 토큰이 만료되었습니다.\"}");
                    return; // 필터 체인 종료
                } catch (Exception e) {
                    log.info("Token validation failed.", e);

                    res.setContentType("application/json");
                    res.setCharacterEncoding("utf-8");
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.getWriter().write("{\"msg\":\"토큰 검증 실패\"}");
                    return; // 필터 체인 종료
                }
            }
        }
        log.info("doFilter");
        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}