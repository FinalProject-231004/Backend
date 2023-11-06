package com.starta.project.domain.answer.service;

import com.starta.project.domain.answer.dto.ChoiceRequestDto;
import com.starta.project.domain.answer.dto.ResultResponseDto;
import com.starta.project.domain.answer.dto.WhatWrongResponseDto;
import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.answer.repository.MemberAnswerRepository;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final QuizChoicesRepository quizChoicesRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;
    private final QuizRepository quizRepository;
    //레디스 사용을 위한 레디스 템플릿
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional     // 퀴즈 선택지 (응답)
    public void choice  (ChoiceRequestDto choiceRequestDto, Member member) {
        //선택지 찾아오기
        QuizChoices quizChoices = findQuizChoice(choiceRequestDto.getChoiceId());
        //필요 변수
        Long quizId = quizChoices.getQuizQuestion().getQuiz().getId();
        Integer quizQuestionNum = quizChoices.getQuizQuestion().getQuestionNum();
        LocalDateTime localDate = LocalDateTime.now();
        MemberDetail memberDetail = member.getMemberDetail();

        //필요한 조건 찾아오기 -> 응답이 있는 지 없는지, 하루의 푼 문제의 숫자
        int daySolve = mileageGetHistoryRepository.countByDateAndMemberDetailAndType(localDate ,memberDetail, TypeEnum.QUIZ_SOLVE);
        Optional<MemberAnswer> answer = memberAnswerRepository.findTopByMemberIdAndQuizQuestionNumAndQuizId(
                member.getId(),quizQuestionNum,quizId);
        //우선 객체 형성
        MemberAnswer memberAnswer = new MemberAnswer();
        //옵션의 결과에 따라 있으면 기존의 응답 변경 | 없다면 푼 문제의 갯수에 따라 10개 보다 작으면 문제를 제작함
        if(answer.isPresent()) {
             memberAnswer = answer.get();
        } else if(answer.isEmpty() && daySolve < 10) {
            Integer i = 10;
            memberDetail.gainMileagePoint(i);
            String des = "퀴즈 문제풀이 참여로 획득";
            MileageGetHistory mileageGetHistory = new MileageGetHistory();
            mileageGetHistory.getFromAnswer(i,des,memberDetail);
            mileageGetHistoryRepository.save(mileageGetHistory);
        }

        //정답 체크
        if (quizChoices.isChecks() == true && memberAnswer.isGetScore() == false) {
            memberDetail.gainScore(10);
            memberAnswer.gainScore(true);
        }
        memberAnswer.set(quizChoices.isChecks());
        //응답 저장
        memberAnswer.answer(quizId,member.getId(),quizQuestionNum, quizChoices);
        // cascade를 위해 응답이 있는 조건과 없는 조건를 두고 상황에 따라 구별하여 저장함
        if (answer.isEmpty()) memberDetail.answer(memberAnswer);
        else {
            memberDetail.changeAnswer(memberAnswer);
        }
        memberDetailRepository.save(memberDetail);
    }

    public void noMemberChoice(ChoiceRequestDto choiceRequestDto, HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException {
        //선택지 찾아오기
        QuizChoices quizChoices = findQuizChoice(choiceRequestDto.getChoiceId());

        //필요 변수
        String quizId = quizChoices.getQuizQuestion().getQuiz().getId().toString();
        String quizQuestionNum = quizChoices.getQuizQuestion().getQuestionNum().toString();
        String hashedIP = hashIP(httpServletRequest);

        //레디스에서 값을 조회함
        List<String> checkList = redisTemplate.opsForList().range(hashedIP+"_0"+quizId, 0, -1 );
        // 정답인 경우 레디스에 값을 저장하고 데이터의 잔존 시간을 30분으로 규정
        if (!(checkList == null)) {
            if (!checkList.contains(quizQuestionNum) && quizChoices.isChecks() ) {
                redisTemplate.opsForList().rightPush(hashedIP + "_0" + quizId, quizQuestionNum);
            }
        } else if (checkList == null && quizChoices.isChecks()) {
            redisTemplate.opsForList().rightPush(hashedIP + "_0" + quizId, quizQuestionNum);
        }
        redisTemplate.expire(hashedIP+"_0"+quizId , 30, TimeUnit.MINUTES);
    }


    //결과창 보기
    @Transactional
    public MsgDataResponse result(Long id, Member member) {

        Quiz quiz = findQuiz(id);
        Long quizId = quiz.getId();

        int totalQuiz = quizQuestionRepository.countByQuiz(quiz);
        int correctQuiz = memberAnswerRepository.countByQuizIdAndCorrectIsTrueAndMemberId(quizId,member.getId());
        ResultResponseDto resultResponseDto = new ResultResponseDto();
        resultResponseDto.set(quiz);

        return new MsgDataResponse
                ( quiz.getTitle()+" 문제에서 " + totalQuiz+ "개의 문제 중 " + correctQuiz +"개 정답! ", resultResponseDto );
    }


    @Transactional
    public MsgDataResponse noMemberResult(Long id, HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException {

        Quiz quiz = findQuiz(id);

        String quizId = id.toString();
        String hashedIP = hashIP(httpServletRequest);
        int correctQuiz = 0;

        List<String> correctQuizList = redisTemplate.opsForList().range(hashedIP + "_0" + quizId, 0, -1);
        if (!(correctQuizList == null)) {
            correctQuiz = correctQuizList.size();
        }

        int totalQuiz = quizQuestionRepository.countByQuiz(quiz);
        ResultResponseDto resultResponseDto = new ResultResponseDto();
        resultResponseDto.set(quiz);
        redisTemplate.expire(hashedIP + "_0" + quizId,0,TimeUnit.SECONDS);

        return new MsgDataResponse (quiz.getTitle()+" 문제에서 " + totalQuiz+ "개의 문제 중 " + correctQuiz +"개 정답! ", resultResponseDto );
    }

    @Transactional
    public MsgDataResponse whatWrong(Long quizId, Member member) {
        Quiz quiz = findQuiz(quizId);
        MemberDetail memberDetail = member.getMemberDetail();


        if(memberDetail.getMileagePoint() < 50) throw new IllegalArgumentException("마일리지가 부족합니다. ");
        else {
            memberDetail.changeMileagePoint(50);
            memberDetailRepository.save(memberDetail);
            MileageGetHistory mileageGetHistory = new MileageGetHistory();
            String des = quiz.getTitle() + "퀴즈 정답 보기";
            mileageGetHistory.spendMileage(des,50,memberDetail);
            mileageGetHistoryRepository.save(mileageGetHistory);
        }
        List<WhatWrongResponseDto> list = new ArrayList<>();
        List<QuizQuestion> quizQuestionList = quizQuestionRepository.findAllByQuiz(quiz);
        for (QuizQuestion quizQuestion : quizQuestionList) {
            MemberAnswer memberAnswer = memberAnswerRepository.findByMemberDetailAndQuizIdAndQuizQuestionNum(memberDetail,quizId,quizQuestion.getQuestionNum());
            QuizChoices quizChoices = quizChoicesRepository.findByQuizQuestionAndChecksIsTrue(quizQuestion);
            WhatWrongResponseDto whatWrongResponseDto = new WhatWrongResponseDto(quizQuestion,quizChoices,memberAnswer);
            list.add(whatWrongResponseDto);
        }

        return new MsgDataResponse(quiz.getTitle()+ " 퀴즈의 정답입니다. 다시 풀어주세요! ", list);
    }


    private Quiz findQuiz (Long id) {
        return quizRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 퀴즈는 없는 퀴즈입니다. ")
        );
    }

    private String hashIP (HttpServletRequest httpServletRequest) throws NoSuchAlgorithmException {
        String ipAddress = httpServletRequest.getRemoteAddr();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(ipAddress.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    private QuizChoices findQuizChoice(Long id) {
        return quizChoicesRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 선택지는 잘못된 선택지입니다. ㅋ "));
    }
}