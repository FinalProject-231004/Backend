package com.starta.project.domain.liveQuiz.service;


import com.starta.project.domain.liveQuiz.component.ActiveUsersManager;
import com.starta.project.domain.liveQuiz.dto.AnswerDto;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final ActiveUsersManager activeUsersManager;
    private String correctAnswer;

    public void setCorrectAnswer(AnswerDto answerDto) {
        MemberDetail findMember = findMemberDetail(answerDto.getNickName());
        UserRoleEnum role = findMember.getMember().getRole();

        if (!(role == UserRoleEnum.ADMIN)) {
            throw new IllegalArgumentException("관리자가 아닙니다.");
        }

        this.correctAnswer = answerDto.getMessage();
    }

    public ChatMessageDto processMessage(ChatMessageDto chatMessage) {
        if (chatMessage != null && chatMessage.getMessage() != null) {
            // 메시지 내용 이스케이프 처리
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
            chatMessage.setMessage(escapedMessage);

            // 메시지가 정답인지 확인
            if (escapedMessage.equalsIgnoreCase(correctAnswer)) {
                // 정답인 경우, 특별한 알림 메시지로 설정
                chatMessage.setMessage((chatMessage.getNickName()) + "님 정답!");
            }
        }
        return chatMessage;
    }

    public Set<String> getCurrentActiveUsers() {
        return activeUsersManager.getUniqueNickNames(); // 변경된 부분
    }

    public String findNickName(String username) {
        Member findMember = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return findMember.getMemberDetail().getNickname();
    }

    private MemberDetail findMemberDetail(String nickName) {
        return memberDetailRepository.findByNickname(nickName).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
