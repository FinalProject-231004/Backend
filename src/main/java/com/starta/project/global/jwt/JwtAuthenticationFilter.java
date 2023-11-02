package com.starta.project.global.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.starta.project.domain.member.dto.LoginRequestDto;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.service.RefreshTokenService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private static final ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        setFilterProcessesUrl("/api/member/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");
        try {
            if (request.getContentLength() <= 0) {
                throw new BadCredentialsException("요청 본문이 비어 있습니다.");
            }
            LoginRequestDto requestDto = mapper.readValue(request.getInputStream(), LoginRequestDto.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error("예외 발생: ", e);
            throw new AuthenticationServiceException("요청 처리 중 오류가 발생했습니다.", e);
        }
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getMember().getRole();

        String token = jwtUtil.createToken(username, role);
        String refreshToken = refreshTokenService.createRefreshToken(username ,role);
        jwtUtil.addJwtToHeader(token,refreshToken, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        String msg = "로그인 실패";
        try(PrintWriter writer = response.getWriter()) {
            String jsonDto = mapper.writeValueAsString(new MsgResponse(msg));
            writer.print(jsonDto);
        } catch (IOException e) {
            log.error("예외 발생: ", e);
            throw new RuntimeException("응답 처리 중 오류가 발생했습니다.");
        }
    }
}