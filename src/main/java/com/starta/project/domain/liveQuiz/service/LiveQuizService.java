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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {
    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final ActiveUsersManager activeUsersManager;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;
    private final RateLimiter rateLimiter = RateLimiter.create(2);
    private final StringRedisTemplate redisTemplate;

    private static final String QUIZ_STATE_KEY = "QuizState";
    private static final String USER_MUTE_TIMES_KEY = "UserMuteTimes";
    private static final String CORRECT_ANSWERED_USERS_KEY = "CorrectAnsweredUsers";

    // 퀴즈 상태 설정
    public void setQuizState(String correctAnswer, Integer winnerCount, Integer mileagePoint) {
        Map<String, String> quizState = new HashMap<>();
        quizState.put("correctAnswer", correctAnswer);
        quizState.put("winnerCount", winnerCount.toString());
        quizState.put("currentWinnersCount", "0");  // 초기화
        quizState.put("mileagePoint", mileagePoint.toString());

        redisTemplate.opsForHash().putAll(QUIZ_STATE_KEY, quizState);
    }

    // 퀴즈 상태 가져오기
    public Map<String, String> getQuizState() {
        return redisTemplate.<String, String>opsForHash().entries(QUIZ_STATE_KEY);
    }

    // 정답 세팅
    public MsgResponse setCorrectAnswer(Member member, AnswerDto answerDto, SimpMessageSendingOperations messagingTemplate) {
        Member findMember = findMember(member.getUsername());
        UserRoleEnum role = findMember.getRole();
        if (!(role == UserRoleEnum.ADMIN)) {
            throw new IllegalArgumentException("관리자가 아닙니다.");
        }

        // 새로운 정답과 정답자 수 설정, 마일리지 포인트
        setQuizState(answerDto.getAnswer(), answerDto.getWinnersCount(), answerDto.getMileagePoint());
        clearCorrectAnsweredUsers();

        sendQuizUpdate(messagingTemplate);
        messagingTemplate.convertAndSend("/topic/liveChatRoom", new ChatMessageDto("공지", findMember.getMemberDetail().getNickname() + "님께서 문제를 출제하였습니다.", LocalDateTime.now()));
        return new MsgResponse("정답이 설정되었습니다.");
    }

    public void muteUser(String nickName) {
        String muteTime = LocalDateTime.now().plusSeconds(30).toString();
        redisTemplate.opsForHash().put(USER_MUTE_TIMES_KEY, nickName, muteTime);
        redisTemplate.expire(USER_MUTE_TIMES_KEY, 30, TimeUnit.SECONDS);
    }

    public LocalDateTime getUserMuteTime(String nickName) {
        String time = (String) redisTemplate.opsForHash().get(USER_MUTE_TIMES_KEY, nickName);
        return time != null ? LocalDateTime.parse(time) : null;
    }

    // 정답을 맞춘 사용자 목록 관리 관련 메서드
    public void addCorrectAnsweredUser(String nickName) {
        redisTemplate.opsForSet().add(CORRECT_ANSWERED_USERS_KEY, nickName);
    }

    public Set<String> getCorrectAnsweredUsers() {
        return redisTemplate.opsForSet().members(CORRECT_ANSWERED_USERS_KEY);
    }

    public void clearCorrectAnsweredUsers() {
        redisTemplate.delete(CORRECT_ANSWERED_USERS_KEY);
    }

    // 현재 정답자 수 업데이트
    private void updateCurrentWinnersCount(int currentWinnersCount) {
        redisTemplate.opsForHash().put(QUIZ_STATE_KEY, "currentWinnersCount", String.valueOf(currentWinnersCount));
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

    // 메시지 처리 메서드
    @Transactional
    public synchronized ChatMessageDto processMessage(ChatMessageDto chatMessage, SimpMessageSendingOperations messagingTemplate) {
        // 사용자 챗금 상태 확인
        LocalDateTime muteExpiry = getUserMuteTime(chatMessage.getNickName());
        if (muteExpiry != null && LocalDateTime.now().isBefore(muteExpiry)) throw new CustomRateLimiterException("채팅 금지 상태입니다.");

        // 메시지 내용 이스케이프 처리
        String escapedMessage = HtmlUtils.htmlEscape(chatMessage.getMessage());
        chatMessage.setMessage(escapedMessage);

        Map<String, String> quizState = getQuizState();
        String correctAnswer = quizState.get("correctAnswer");
        int winnerCount = Integer.parseInt(quizState.get("winnerCount"));
        int currentWinnersCount = Integer.parseInt(quizState.get("currentWinnersCount"));

        Set<String> correctAnsweredUsers = getCorrectAnsweredUsers();

        // 정답을 맞춘 상태에서 정답을 스포할 경우
        if (escapedMessage.equalsIgnoreCase(correctAnswer) && correctAnsweredUsers.contains(chatMessage.getNickName())) {
            return new ChatMessageDto(chatMessage.getNickName(), "이미 정답을 맞추셨습니다!", LocalDateTime.now());
        }

        // 정답 맞췄을 때
        if (escapedMessage.equalsIgnoreCase(correctAnswer) && currentWinnersCount < winnerCount && !correctAnsweredUsers.contains(chatMessage.getNickName())) {
            addCorrectAnsweredUser(chatMessage.getNickName());
            currentWinnersCount++;
            updateCurrentWinnersCount(currentWinnersCount);

            sendQuizUpdate(messagingTemplate);

            // 포인트 지급
            awardMileagePoints(chatMessage.getNickName());
            sendAnswerNotification(chatMessage, messagingTemplate);

            int remainingWinners = winnerCount - currentWinnersCount;
            if (remainingWinners > 0) {
                sendRemainingWinnersNotification(remainingWinners, messagingTemplate);
            } else {
                sendAllWinnersNotification(messagingTemplate);
            }
            return null;
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
        Map<String, String> quizState = getQuizState();
        Set<String> correctAnsweredUsers = getCorrectAnsweredUsers();

        int winnerCount = Integer.parseInt(quizState.get("winnerCount"));
        int currentWinnersCount = Integer.parseInt(quizState.get("currentWinnersCount"));
        int mileagePoint = Integer.parseInt(quizState.get("mileagePoint"));
        String correctAnswer = quizState.get("correctAnswer");

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
        String correctAnswer = getQuizState().get("correctAnswer");
        ChatMessageDto allWinnersMessage = new ChatMessageDto("공지", "모든 정답자가 나왔습니다. 정답은 " + correctAnswer + " 입니다", LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/liveChatRoom", allWinnersMessage);
    }

    // 마일리지 지급
    @Transactional
    public void awardMileagePoints(String nickName) {
        MemberDetail findMember = findMemberDetail(nickName);
        int mileagePoint = Integer.parseInt(getQuizState().get("mileagePoint"));
        findMember.gainMileagePoint(mileagePoint);
        MileageGetHistory mileageGetHistory = new MileageGetHistory("라이브 퀴즈 정답 포인트", TypeEnum.LIVE_QUIZ, mileagePoint, findMember);
        mileageGetHistoryRepository.save(mileageGetHistory);
    }

    public LiveQuizUserInfoDto liveQuizUserInfo(Member member) {
        Member findMember = findMember(member.getUsername());
        Map<String, String> quizState = getQuizState();
        Set<String> correctAnsweredUsers = getCorrectAnsweredUsers();

        int winnerCount = Integer.parseInt(quizState.get("winnerCount"));
        int currentWinnersCount = Integer.parseInt(quizState.get("currentWinnersCount"));
        int mileagePoint = Integer.parseInt(quizState.get("mileagePoint"));
        String correctAnswer = quizState.get("correctAnswer");

        QuizUpdateDto quizUpdate = new QuizUpdateDto(
                correctAnsweredUsers,
                winnerCount - currentWinnersCount,
                correctAnswer.length(),
                mileagePoint
        );
        return new LiveQuizUserInfoDto(findMember.getRole(), findMember.getMemberDetail().getNickname(), quizUpdate);
    }

    private Member findMember(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    public String findNickName(String username) {
        Member findMember = memberRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return findMember.getMemberDetail().getNickname();
    }

    // 현재 접속자 명단
    public Set<String> getCurrentActiveUsers() {
        return activeUsersManager.getUniqueNickNames();
    }

    private MemberDetail findMemberDetail(String nickName) {
        return memberDetailRepository.findByNickname(nickName).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
