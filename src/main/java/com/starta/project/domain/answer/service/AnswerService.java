package com.starta.project.domain.answer.service;

import com.starta.project.domain.answer.dto.ChoiceRequestDto;
import com.starta.project.domain.answer.dto.ResultResponseDto;
import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.answer.repository.MemberAnswerRepository;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final QuizChoicesRepository quizChoicesRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final CommentRepository commentRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;
    private final QuizRepository quizRepository;


    @Transactional     // 퀴즈 선택지 (응답)
    public void choice(ChoiceRequestDto choiceRequestDto, Member member) {
        //선택지 찾아오기
        QuizChoices quizChoices = quizChoicesRepository.findById(choiceRequestDto.getChoiceId()).orElseThrow(
                () -> new NullPointerException("해당 선택지는 잘못된 선택지입니다. ㅋ "));
        //필요 변수
        Long quizId = quizChoices.getQuizQuestion().getQuiz().getId();
        Integer quizQuestionNum = quizChoices.getQuizQuestion().getQuestionNum();
        LocalDateTime localDate = LocalDateTime.now();
        MemberDetail memberDetail = member.getMemberDetail();

        //필요한 조건 찾아오기 -> 응답이 있는 지 없는지, 하루의 푼 문제의 숫자
        Optional<MemberAnswer> answer = memberAnswerRepository.findTopByMemberIdAndQuizQuestionNumAndQuizId(
                member.getId(),quizQuestionNum,quizId);
        int daySolve = mileageGetHistoryRepository.countByDateAndMemberDetailAndType(localDate,memberDetail, TypeEnum.QUIZ_SOLVE);

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
        memberAnswer.answer(quizId,member.getId(),quizQuestionNum);
        // cascade를 위해 응답이 있는 조건과 없는 조건를 두고 상황에 따라 구별하여 저장함
        if (answer.isEmpty()) memberDetail.answer(memberAnswer);
        else {
            memberDetail.changeAnswer(memberAnswer);
        }
        memberDetailRepository.save(memberDetail);
    }

    //결과창 보기
    public ResponseEntity<MsgDataResponse> result(Long id, Member member) {

        Quiz quiz = quizRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 퀴즈는 없는 퀴즈입니다. ")
        );
        Long quizId = quiz.getId();
        List<Comment> List = commentRepository.findAllByQuizId(quizId);

        int totalQuiz = quizQuestionRepository.countByQuiz(quiz);
        int correctQuiz = memberAnswerRepository.countByQuizIdAndCorrectIsTrueAndMemberId(quizId,member.getId());
        ResultResponseDto resultResponseDto = new ResultResponseDto();
        resultResponseDto.set(quiz, List);

        return ResponseEntity.ok(new MsgDataResponse
                ( quiz.getTitle()+" 문제에서 " + totalQuiz+ "개의 문제 중 " + correctQuiz +"개 정답! ", resultResponseDto ));
    }

}