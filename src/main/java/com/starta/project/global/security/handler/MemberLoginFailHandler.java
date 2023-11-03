package com.starta.project.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MemberLoginFailHandler implements AuthenticationFailureHandler {

    private static final ObjectMapper mapper = new ObjectMapper();


    private void setErrorResponse(HttpServletResponse res, int statusCode, String msg) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json;charset=UTF-8");
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("msg", msg);
        res.getWriter().write(mapper.writeValueAsString(errorDetails));
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception instanceof UsernameNotFoundException) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "계정이 존재하지 않습니다.");
        } else if (exception instanceof BadCredentialsException) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "아이디 또는 비밀번호가 맞지 않습니다.");
        } else if (exception instanceof InternalAuthenticationServiceException) {
            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "내부적으로 발생한 시스템 문제로 인해 요청을 처리할 수 없습니다. 관리자에게 문의하세요.");
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            setErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "인증 요청이 거부되었습니다. 관리자에게 문의하세요.");
        } else {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "알 수 없는 이유로 로그인에 실패하였습니다. 관리자에게 문의하세요.");
        }
    }
}