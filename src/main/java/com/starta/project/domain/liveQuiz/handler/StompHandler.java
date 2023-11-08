package com.starta.project.domain.liveQuiz.handler;

import com.starta.project.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
//public class StompHandler implements ChannelInterceptor {
//
//    private final JwtUtil jwtUtil;
//
//    @Autowired
//    public StompHandler(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        List<String> authorizationHeaderList = accessor.getNativeHeader("Authorization");
//        if (StompCommand.CONNECT == accessor.getCommand()) {
//            if (authorizationHeaderList != null && !authorizationHeaderList.isEmpty()) {
//                String bearerToken = authorizationHeaderList.get(0); // Bearer 토큰 값
//                if (bearerToken.startsWith(JwtUtil.BEARER_PREFIX)) {
//                    String token = bearerToken.substring(JwtUtil.BEARER_PREFIX.length());
//
//                    if (jwtUtil.validateToken(token)) {
//                        Claims claims = jwtUtil.getUserInfoFromToken(token);
//                        String username = claims.getSubject();
//
//                        // 권한 정보 등을 포함하여 인증 객체를 생성할 수 있습니다.
//                        UsernamePasswordAuthenticationToken authentication =
//                                new UsernamePasswordAuthenticationToken(username, null, null); // roles can be extracted if needed
//
////                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                        // 여기서부터 사용자가 인증되었다고 가정하고, 필요한 비즈니스 로직을 수행할 수 있습니다.
//                        System.out.println("인증된 사용자: " + username);
//                        System.out.println(authentication.getPrincipal());
//                        accessor.setUser(authentication);
//                        System.out.println("accessor : " + accessor.getUser());
//                    }
//                }
//            }
//        }
//
//        return message;
//    }
//}
