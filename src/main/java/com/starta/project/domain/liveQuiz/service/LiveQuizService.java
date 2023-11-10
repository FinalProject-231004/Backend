package com.starta.project.domain.liveQuiz.service;


import com.google.common.util.concurrent.RateLimiter;
import com.starta.project.domain.liveQuiz.component.ActiveUsersManager;
import com.starta.project.domain.liveQuiz.dto.AnswerDto;
import com.starta.project.domain.liveQuiz.dto.ChatMessageDto;
import com.starta.project.domain.liveQuiz.dto.LiveQuizUserInfoDto;
import com.starta.project.domain.liveQuiz.dto.QuizUpdateDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.global.exception.custom.CustomRateLimiterException;
import com.starta.project.global.exception.custom.CustomUserBlockedException;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private Set<String> correctAnsweredUsers = new HashSet<>();

    // 정답 세팅
    public MsgResponse setCorrectAnswer(Member member, AnswerDto answerDto, SimpMessageSendingOperations messagingTemplate) {
        Member findMember = findMember(member.getUsername());
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
        sendQuizUpdate(messagingTemplate);
        messagingTemplate.convertAndSend("/topic/liveChatRoom", new ChatMessageDto("공지",findMember.getMemberDetail().getNickname() + "님께서 문제를 출제하였습니다.", LocalDateTime.now()));
        return new MsgResponse("정답이 설정되었습니다.");
    }

    @Transactional
    public ChatMessageDto processIncomingMessage(ChatMessageDto chatMessage, SimpMessageSendingOperations messagingTemplate) {
        try {
            UserRoleEnum role = memberRepository.findUserRoleByNickName(chatMessage.getNickName());
            // 사용자가 BLOCK 상태인 경우, 메시지 전송 차단
            if (role == UserRoleEnum.BLOCK) {
                throw new CustomUserBlockedException("차단된 유저입니다.");
            }

            if (!rateLimiter.tryAcquire()) {
                muteUser(chatMessage.getNickName());
                System.out.println("도배자" + chatMessage.getNickName() + "차단됨");
                throw new CustomRateLimiterException("도배 금지!");
            }
        } catch (CustomUserBlockedException | CustomRateLimiterException e) {
            // 에러 메시지 생성 및 전송 로직
            ChatMessageDto errorResponse = createErrorResponse(chatMessage.getNickName(), e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getNickName(),
                    "/queue/errors",
                    errorResponse);
            return errorResponse;
        }
        return processMessage(chatMessage, messagingTemplate);
    }

    @Transactional
    public synchronized ChatMessageDto processMessage(ChatMessageDto chatMessage, SimpMessageSendingOperations messagingTemplate) {
        // 사용자 챗금 상태 확인
        LocalDateTime muteExpiry = userMuteTimes.get(chatMessage.getNickName());
        if (muteExpiry != null && LocalDateTime.now().isBefore(muteExpiry)) throw new CustomRateLimiterException("채팅 금지 상태입니다.");
        if (chatMessage != null && chatMessage.getMessage() != null) {
            // 메시지 내용 이스케이프 처리
            String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
            chatMessage.setMessage(escapedMessage);

            // 정답을 맞춘 상태에서 정답을 스포할 경우
            if (escapedMessage.equalsIgnoreCase(correctAnswer) && correctAnsweredUsers.contains(chatMessage.getNickName())) {
                chatMessage = new ChatMessageDto(chatMessage.getNickName(), chatMessage.getNickName() + "님 이미 정답을 맞추셨습니다!", LocalDateTime.now());
                return chatMessage;
            }

            // 정답 맞췄을 때
            if (escapedMessage.equalsIgnoreCase(correctAnswer) && currentWinnersCount < winnerCount && !correctAnsweredUsers.contains(chatMessage.getNickName())) {
                correctAnsweredUsers.add(chatMessage.getNickName());
                currentWinnersCount++;
                sendQuizUpdate(messagingTemplate);

                // 포인트 지급
                awardMileagePoints(chatMessage.getNickName());
                sendAnswerNotification(chatMessage, messagingTemplate);

                int remainingWinners = winnerCount - currentWinnersCount;
                if (remainingWinners > 0) {sendRemainingWinnersNotification(remainingWinners, messagingTemplate);}
                else {sendAllWinnersNotification(messagingTemplate);}
                return null;}
        }
        return new ChatMessageDto(chatMessage.getNickName(), chatMessage.getMessage(), LocalDateTime.now());
    }

    // 에러 메시지 생성
    private ChatMessageDto createErrorResponse(String nickName, String errorMessage) {
        return new ChatMessageDto(
                nickName,
                errorMessage,
                LocalDateTime.now(),
                ChatMessageDto.MessageType.ERROR
        );
    }

    // 정답자 목록 업데이트
    private void sendQuizUpdate(SimpMessageSendingOperations messagingTemplate) {
        QuizUpdateDto quizUpdate = new QuizUpdateDto(
                correctAnsweredUsers,
                winnerCount - currentWinnersCount,
                correctAnswer.length(),
                mileagePoint
        );
        messagingTemplate.convertAndSend("/topic/quizUpdate", quizUpdate);
    }

    // 정답 알림
    private void sendAnswerNotification(ChatMessageDto chatMessage, SimpMessageSendingOperations messagingTemplate) {
        ChatMessageDto answerMessage = new ChatMessageDto(chatMessage.getNickName(), chatMessage.getNickName() + "님 정답!", LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/liveChatRoom", answerMessage);
    }

    // 남은 정답자 알림
    private void sendRemainingWinnersNotification(int remainingWinners, SimpMessageSendingOperations messagingTemplate) {
        ChatMessageDto remainingWinnersMessage = new ChatMessageDto("공지", "남은 정답자는 " + remainingWinners + "명 입니다.", LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/liveChatRoom", remainingWinnersMessage);
    }

    // 모든 정답자 알림
    private void sendAllWinnersNotification(SimpMessageSendingOperations messagingTemplate) {
        ChatMessageDto allWinnersMessage = new ChatMessageDto("공지", "모든 정답자가 나왔습니다. 정답은 " +correctAnswer +" 입니다" , LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/liveChatRoom", allWinnersMessage);
    }

    // 마일리지 지급
    @Transactional
    public void awardMileagePoints(String nickName) {
        MemberDetail findMember = findMemberDetail(nickName);
        findMember.gainMileagePoint(mileagePoint);
        MileageGetHistory mileageGetHistory = new MileageGetHistory("라이브 퀴즈 정답 포인트", TypeEnum.LIVE_QUIZ, mileagePoint, findMember);
        mileageGetHistoryRepository.save(mileageGetHistory);
    }

    public LiveQuizUserInfoDto liveQuizUserInfo(Member member) {
        Member findMember = findMember(member.getUsername());
        return new LiveQuizUserInfoDto(findMember.getRole(), findMember.getMemberDetail().getNickname());
    }

    private Member findMember(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    public String findNickName(String username) {
        Member findMember = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return findMember.getMemberDetail().getNickname();
    }

    // 사용자를 금지 상태로 설정하는 메서드
    public void muteUser(String nickName) {
        userMuteTimes.put(nickName, LocalDateTime.now().plusSeconds(30));
    }

    // 현재 접속자 명단
    public Set<String> getCurrentActiveUsers() {
        return activeUsersManager.getUniqueNickNames();
    }

    private MemberDetail findMemberDetail(String nickName) {
        return memberDetailRepository.findByNickname(nickName).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
