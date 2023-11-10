package com.starta.project.domain.liveQuiz.service;


import com.starta.project.domain.liveQuiz.component.ActiveUsersManager;
import com.starta.project.domain.liveQuiz.dto.AnswerDto;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final ActiveUsersManager activeUsersManager;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;

    private String correctAnswer;
    private int winnerCount = 0;
    private int currentWinnersCount = 0;
    private int mileagePoint = 0;
    private Set<String> correctAnsweredUsers = new HashSet<>(); // 정답을 맞춘 사용자들의 목록


    public void setCorrectAnswer(AnswerDto answerDto) {
        MemberDetail findMember = findMemberDetail(answerDto.getNickName());
        UserRoleEnum role = findMember.getMember().getRole();

        if (!(role == UserRoleEnum.ADMIN)) {
            throw new IllegalArgumentException("관리자가 아닙니다.");
        }

        // 새로운 문제가 설정될 때, 정답자 목록과 카운트를 초기화
        this.correctAnsweredUsers.clear();
        this.currentWinnersCount = 0;

        // 새로운 정답과 정답자 수 설정, 마일리지 포인트
        this.correctAnswer = answerDto.getMessage();
        this.winnerCount = answerDto.getWinnersCount();
        this.mileagePoint = answerDto.getMileagePoint();
    }

    @Transactional
    public synchronized ChatMessageDto processMessage(ChatMessageDto chatMessage) {
        if (chatMessage != null && chatMessage.getMessage() != null) {
            // 메시지 내용 이스케이프 처리
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
            chatMessage.setMessage(escapedMessage);

            // 정답을 맞춘 상태에서 정답을 스포할 경우
            if (escapedMessage.equalsIgnoreCase(correctAnswer) && correctAnsweredUsers.contains(chatMessage.getNickName())) {
                chatMessage = new ChatMessageDto(chatMessage.getNickName(), (chatMessage.getNickName()) + "님 이미 정답을 맞추셨습니다!", LocalDateTime.now());
            }

            // 정답 맞췄을 때
            if (escapedMessage.equalsIgnoreCase(correctAnswer) && currentWinnersCount < winnerCount && !correctAnsweredUsers.contains(chatMessage.getNickName())) {
                correctAnsweredUsers.add(chatMessage.getNickName());
                chatMessage = new ChatMessageDto(chatMessage.getNickName(), (chatMessage.getNickName()) + "님 정답!", LocalDateTime.now());
                currentWinnersCount++;
                // 포인트 지급
                awardMileagePoints(chatMessage.getNickName());
            }

        }
        return chatMessage;
    }

    // 마일리지 지급
    @Transactional
    public void awardMileagePoints(String nickName) {
        MemberDetail findMember = findMemberDetail(nickName);
        findMember.gainMileagePoint(mileagePoint);
        System.out.println("정답자 : " + nickName + " / 마일리지 : " + mileagePoint);
        MileageGetHistory mileageGetHistory = new MileageGetHistory("라이브 퀴즈 정답 포인트", TypeEnum.LIVE_QUIZ, mileagePoint, findMember);
        mileageGetHistoryRepository.save(mileageGetHistory);
    }

    // 현재 접속자 명단
    public Set<String> getCurrentActiveUsers() {
        return activeUsersManager.getUniqueNickNames();
    }

    public String findNickName(String username) {
        Member findMember = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return findMember.getMemberDetail().getNickname();
    }

    private MemberDetail findMemberDetail(String nickName) {
        return memberDetailRepository.findByNickname(nickName).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

}
