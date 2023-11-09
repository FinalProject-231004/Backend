package com.starta.project.domain.liveQuiz.service;


import com.starta.project.domain.liveQuiz.component.ActiveUsersManager;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {

    private final MemberRepository memberRepository;
    private final ActiveUsersManager activeUsersManager;

    public String findNickName(String username) {
        Member findMember = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return findMember.getMemberDetail().getNickname();
    }

    public ChatMessageDto sendMessage(ChatMessageDto chatMessage) {
        if (chatMessage != null && chatMessage.getMessage() != null) {
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
            chatMessage = new ChatMessageDto(chatMessage.getNickName(), escapedMessage, LocalDateTime.now());
        }
        return chatMessage;
    }

    public Set<String> getCurrentActiveUsers() {
        return activeUsersManager.getUniqueNickNames(); // 변경된 부분
    }
}
