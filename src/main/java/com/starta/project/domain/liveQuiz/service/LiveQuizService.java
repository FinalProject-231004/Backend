package com.starta.project.domain.liveQuiz.service;


import com.google.common.util.concurrent.RateLimiter;
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
import com.starta.project.global.exception.custom.CustomRateLimiterException;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final ActiveUsersManager activeUsersManager;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    private String correctAnswer;
    private Integer winnerCount;
    private Integer currentWinnersCount;
    private Integer mileagePoint;
    private final Map<String, LocalDateTime> userMuteTimes = new ConcurrentHashMap<>();
    private Set<String> correctAnsweredUsers = new HashSet<>(); // 정답을 맞춘 사용자들의 목록

    // 정답 세팅
    public MsgResponse setCorrectAnswer(Member member, AnswerDto answerDto) {
        Member findMember = memberRepository.findByUsername(member.getUsername()).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        UserRoleEnum role = findMember.getRole();
        if (!(role == UserRoleEnum.ADMIN)) {
            throw new IllegalArgumentException("관리자가 아닙니다.");
        }

        // 새로운 문제가 설정될 때, 정답자 목록과 카운트를 초기화
        this.correctAnsweredUsers.clear();
        this.currentWinnersCount = 0;

        // 새로운 정답과 정답자 수 설정, 마일리지 포인트
        this.correctAnswer = answerDto.getAnswer();
        this.winnerCount = answerDto.getWinnersCount();
        this.mileagePoint = answerDto.getMileagePoint();

        return new MsgResponse("정답이 설정되었습니다.");
    }

    @Transactional
    public ChatMessageDto processIncomingMessage(ChatMessageDto chatMessage, SimpMessageSendingOperations messagingTemplate) {
        try {
            if (!rateLimiter.tryAcquire()) {
                muteUser(chatMessage.getNickName());
                throw new CustomRateLimiterException("도배 금지!");
            }
        } catch (CustomRateLimiterException e) {
            // 에러 메시지 생성 및 전송 로직
            ChatMessageDto errorResponse = createErrorResponse(chatMessage.getNickName(), e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getNickName(),
                    "/queue/errors",
                    errorResponse
            );
            return errorResponse;
        }
        return processMessage(chatMessage);
    }

    @Transactional
    public synchronized ChatMessageDto processMessage(ChatMessageDto chatMessage) {

        // 사용자 챗금 상태 확인
        LocalDateTime muteExpiry = userMuteTimes.get(chatMessage.getNickName());
        if (muteExpiry != null && LocalDateTime.now().isBefore(muteExpiry)) {
            throw new CustomRateLimiterException("채팅 금지 상태입니다.");
        }

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
        return new ChatMessageDto(chatMessage.getNickName(), chatMessage.getMessage(), LocalDateTime.now());
    }

    // 사용자를 금지 상태로 설정하는 메서드
    public void muteUser(String nickName) {
        userMuteTimes.put(nickName, LocalDateTime.now().plusSeconds(30));
    }

    private ChatMessageDto createErrorResponse(String nickName, String errorMessage) {
        return new ChatMessageDto(
                nickName,
                errorMessage,
                LocalDateTime.now(),
                ChatMessageDto.MessageType.ERROR
        );
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
        System.out.println("닉네임 : " + nickName);
        return memberDetailRepository.findByNickname(nickName).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

}
