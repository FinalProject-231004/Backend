package com.starta.project.domain.answer.service;

import com.starta.project.domain.answer.dto.ResultResponseDto;
import com.starta.project.domain.answer.entity.MemberAnswer;
import com.starta.project.domain.answer.repository.MemberAnswerRepository;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional     // 퀴즈 선택지 (응답)
    public void choice(Long id, Member member) {
        QuizChoices quizChoices = quizChoicesRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 선택지는 잘못된 선택지입니다.! "));
        MemberDetail memberDetail = member.getMemberDetail();
        Optional<MemberAnswer> answer = memberAnswerRepository.findByMemberIdAndQuizQuestionNum(member.getId() ,
                quizChoices.getQuizQuestion().getQuestionNum());

        MemberAnswer memberAnswer = new MemberAnswer();
        if(answer.isPresent()) {
             memberAnswer = answer.get();
        } else if(answer.isEmpty()) {
            Integer i = 10;
            memberDetail.gainMileagePoint(i);
            String des = "퀴즈 문제풀이 참여로 획득";
            MileageGetHistory mileageGetHistory = new MileageGetHistory();
            mileageGetHistory.getFromAnswer(i,des,memberDetail);
            mileageGetHistoryRepository.save(mileageGetHistory);
        }

        //정답 체크
        if (quizChoices.isChecks() == true &&
                (memberAnswer.isCorrect() == false || answer.isEmpty())) {
            memberDetail.gainScore(10);
        }

        System.out.println(memberDetail.getTotalScore());
        memberAnswer.set(quizChoices.isChecks());
        //응답 저장
        Long quizId = quizChoices.getQuizQuestion().getQuiz().getId();
        memberAnswer.answer(member.getId(),quizId,quizChoices.getQuizQuestion().getQuestionNum());

        if (answer.isEmpty()) memberDetail.answer(memberAnswer);
        else {
            memberDetail.changeAnswer(memberAnswer);
        }
        memberDetailRepository.save(memberDetail);
    }

    //결과창 보기
    public ResponseEntity<MsgDataResponse> result(Long id, Member member) {
                        QuizQuestion quizQuestion = quizQuestionRepository.findById(id).orElseThrow(
                        () -> new NullPointerException("해당 문제는 잘못된 문제입니다.! ")
                );
        Quiz quiz = quizQuestion.getQuiz();
        Long quizId = quiz.getId();
        List<Comment> List = commentRepository.findAllByQuizId(quizId);

        int totalQuiz = quizQuestion.getQuestionNum();
        int correctQuiz = memberAnswerRepository.countByQuizIdAndCorrectIsTrueAndMemberId(quizId,member.getId());
        ResultResponseDto resultResponseDto = new ResultResponseDto();
        resultResponseDto.set(quiz, List);

        return ResponseEntity.ok(new MsgDataResponse
                ( quiz.getTitle()+"문제에서 " + totalQuiz+ "개의 문제 중 " + correctQuiz +"개 정답! ", resultResponseDto ));
    }
}
