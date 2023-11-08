package com.starta.project.domain.liveQuiz.service;


import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {


    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        log.warn("chatMessage : {}", chatMessage);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        Member member = userDetails.getMember();

//        System.out.println(member.getId());
        if (chatMessage != null && chatMessage.getMessage() != null) {
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
//            chatMessage = new ChatMessageDto(member.getId(), member.getUsername(), escapedMessage, LocalDateTime.now());
        }
        return chatMessage;
    }
}
