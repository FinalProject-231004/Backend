package com.starta.project.global.jwt;

import com.starta.project.global.exception.custom.CustomExpiredJwtException;
import com.starta.project.global.exception.custom.CustomInvalidJwtException;
import com.starta.project.global.exception.custom.CustomMalformedJwtException;
import com.starta.project.global.exception.custom.CustomUnsupportedJwtException;
import com.starta.project.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

    private void setErrorResponse(HttpServletResponse res, int statusCode, String msg) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("utf-8");
        res.setStatus(statusCode);
        res.getWriter().write("{\"msg\":\"" + msg + "\"}");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT 필터 시작");
        String accessTokenValue = jwtUtil.getTokenFromHeader(req);
        String refreshTokenValue = jwtUtil.getRefreshTokenFromHeader(req);
        String requestURI = req.getRequestURI();

        // 리프레시 토큰 재발급 API
        if ("/api/token/reissue".equals(requestURI) && refreshTokenValue != null) {
            try {
                jwtUtil.checkUsingRefreshToken(refreshTokenValue, res);
                return;
            }  catch (CustomInvalidJwtException e) {
                setErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Expired Refresh Token. 유효하지 않은 JWT 토큰 입니다.");
            }catch (JwtException jwtEx) {
                setErrorResponse(res, HttpServletResponse.SC_UNAUTHORIZED, "Expired Refresh Token. 토큰이 만료되었습니다");
                return;
            }
        }

        // 로그인 또는 재발급 요청이 아닌 경우
        if (!(requestURI.equals("/") || requestURI.equals("/api/member/login") || requestURI.equals("/api/member/signup"))) {
            if (StringUtils.hasText(accessTokenValue)) {
                accessTokenValue = jwtUtil.substringToken(accessTokenValue);
                log.info("validateToken 시작");

                try {
                    if (jwtUtil.validateToken(accessTokenValue)) {
                        Claims info = jwtUtil.getUserInfoFromToken(accessTokenValue);
                        log.info("Claims info" + info);
                        setAuthentication(info.getSubject());
                        log.info("setAuthentication");
                    }
                } catch (CustomExpiredJwtException e) {
                    setErrorResponse(res, 401, "Expired Access Token. 토큰이 만료되었습니다");
                    return;
                } catch (CustomUnsupportedJwtException e) {
                    setErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Unsupported JWT Token. 지원하지 않는 JWT 토큰입니다.");
                    return;
                } catch (CustomMalformedJwtException e) {
                    setErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Malformed JWT Token. 형식이 잘못된 JWT 토큰입니다.");
                    return;
                }catch (CustomInvalidJwtException e) {
                    setErrorResponse(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT signature, 유효하지 않은 JWT 토큰 입니다.");
                    return;
                } catch (Exception e) {
                    setErrorResponse(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "토큰을 다시 발급해주세요.");
                    return;
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