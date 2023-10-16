package com.starta.project.global.jwt;


import com.starta.project.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
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
        String servletPath = req.getServletPath();
        //home 화면은 토큰 체크 x
        if(!(servletPath.equals("/") || servletPath.equals("/api/member/login") || servletPath.equals("/api/member/reissue"))) {
            //access 토큰 값
            String accessTokenValue = jwtUtil.getTokenFromHeader(req);
            String refreshTokenValue = jwtUtil.getRefreshTokenFromHeader(req);

            if (StringUtils.hasText(accessTokenValue)) {
                // JWT 토큰 substring
                accessTokenValue = jwtUtil.substringToken(accessTokenValue);
                log.info("validateToken 시작");
                //access 토큰이 유효하면 그대로 반환, 만료되어 refresh토큰 통해 반환되면 새로운 토큰 발급

                if(!jwtUtil.validateToken(accessTokenValue, refreshTokenValue, res)){
                    log.error("Token Error");
                    return;
                }


//                accessTokenValue = token;

                Claims info = jwtUtil.getUserInfoFromToken(accessTokenValue);

                try {
                    setAuthentication(info.getSubject());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
            }
        }

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