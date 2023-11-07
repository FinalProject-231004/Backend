package com.starta.project.domain.quiz.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.quiz.dto.*;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.entity.QuizChoices;
import com.starta.project.domain.quiz.entity.QuizQuestion;
import com.starta.project.domain.quiz.repository.QuizChoicesRepository;
import com.starta.project.domain.quiz.repository.QuizQuestionRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.aws.AmazonS3Service;
import com.starta.project.global.messageDto.MsgResponse;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {

    private final QuizRepository quizRepository;
    private final QuizChoicesRepository quizChoicesRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final AmazonS3Service amazonS3Service;
    private final MileageGetHistoryRepository getHistoryRepository;
    private final MemberDetailRepository memberDetailRepository;

    //퀴즈 생성
    @Transactional
    public ResponseEntity<MsgResponse> createQuizQuestion(
            Long id,
            List<MultipartFile> images,
            List<CreateQuestionRequestDto> questionListRequestDto,
            Member member) {

        // 권한 체크
        if (member.getRole() == UserRoleEnum.BLOCK) {
            throw new IllegalArgumentException("신고 누적으로 퀴즈 생성이 권한이 차단되었습니다.");
        }

        //퀴즈 찾기
        Quiz quiz = findQuiz(id);

        //퀴즈 생성자 확인
        if (!member.getId().equals(quiz.getMemberId())) {
            MsgResponse msgResponse = new MsgResponse("퀴즈 생성자가 아닙니다. ");
            return ResponseEntity.badRequest().body(msgResponse);
        }

        int imageIndex = 0;
        for (CreateQuestionRequestDto questionDto : questionListRequestDto) {

            //이미지 추가
            String img = "";
            if (imageIndex < images.size()) {
                MultipartFile imageFile = images.get(imageIndex);
                if (!imageFile.getOriginalFilename().equals("noimage01.jpg")) {
                    try {
                        img = amazonS3Service.upload(imageFile);
                    } catch (java.io.IOException e) {
                        throw new RuntimeException("이미지 업로드에 문제가 있습니다!  "); // 또는 적절한 예외 클래스를 사용하세요.
                    }
                }
            }

            //문제 번호 찾기
            Integer questionNum = 0;
            Optional<QuizQuestion> question = quizQuestionRepository.findTopByQuizOrderByQuestionNumDesc(quiz);
            if (question.isPresent()){
                questionNum = question.get().getQuestionNum();
            }

            //문제 만들기
            QuizQuestion quizQuestion = new QuizQuestion();
            questionNum++;
            quizQuestion.set(quiz, questionNum, questionDto.getTitle(), img);
            quizQuestionRepository.save(quizQuestion);

            //선택지 만들기
            List<CreateQuizChoicesDto> quizChoicesList = questionDto.getQuizChoices();
            List<QuizChoices> quizChoices = new ArrayList<>();
            for (CreateQuizChoicesDto createQuizChoicesDto : quizChoicesList) {
                QuizChoices quizChoice = new QuizChoices();
                quizChoice.set(createQuizChoicesDto, quizQuestion);
                quizChoices.add(quizChoice);
            }
            quizChoicesRepository.saveAll(quizChoices);

            imageIndex++;
        }
        quiz.playOn(true);
        LocalDateTime localDate = LocalDateTime.now();

        Optional<MileageGetHistory> getHistory = getHistoryRepository.findFirstByDateAndMemberDetailAndType(localDate,
                member.getMemberDetail(),TypeEnum.QUIZ_CREATE);

        if(getHistory.isEmpty()){
            MemberDetail memberDetail = member.getMemberDetail();
            Integer i = 50;
            memberDetail.gainMileagePoint(i);
            memberDetailRepository.save(memberDetail);
            MileageGetHistory mileageGetHistory = new MileageGetHistory();
            String des = "오늘의 퀴즈 생성";
            mileageGetHistory.getFromQuiz(memberDetail,i,des);
            getHistoryRepository.save(mileageGetHistory);
        }

        quizRepository.save(quiz);
        return ResponseEntity.ok(new MsgResponse("문제 생성을 성공 하셨습니다!"));
    }

    //문항 한 문제씩 보기
    public ShowQuestionResponseDto showQuizQuestion(Long id, Integer questionNum) {
        // 퀴즈 찾기
        Quiz quiz = findQuiz(id);
        // 해당 퀴즈의 n번 문제 찾기
        QuizQuestion quizQuestion = findQuizQuestion(quiz, questionNum);
        // 선택지 찾아오기
        List<QuizChoices> quizChoicesList = quizChoicesRepository.findAllByQuizQuestion(quizQuestion);

        List<ChoicesList> list = new ArrayList<>();
        for (QuizChoices quizChoices : quizChoicesList) {
            ChoicesList choicesList = new ChoicesList(quizChoices);
            list.add(choicesList);
        }
        // 반환
        ShowQuestionResponseDto showQuestionResponseDto = new ShowQuestionResponseDto();
        showQuestionResponseDto.set(quizQuestion, list);
        return showQuestionResponseDto;
    }

    // cascade를 사용하는 방식도 있지만 이용하기 위해서는 DB에 추가적 연관관계를 설정해야함
    // cascade와 같은 경우 강력한 기능이지만 생각 못한 상황에서 삭제될 가능성이 있기 때문에 그냥 단순 조회를 통해 찾아서 지움
    //퀴즈 삭제
    @Transactional
    public ResponseEntity<MsgResponse> deleteQuizQuestion(Long id, Integer questionNum, Member member) {
        // 퀴즈 찾기
        Quiz quiz = findQuiz(id);

        if (!member.getId().equals(quiz.getMemberId())) {
            MsgResponse msgResponse = new MsgResponse("퀴즈 생성자가 아닙니다. ");
            return ResponseEntity.badRequest().body(msgResponse);
        }
        // 해당 퀴즈의 n번 문제 찾기
        QuizQuestion quizQuestion = findQuizQuestion(quiz, questionNum);
        //이미지 삭제 S3
        try {
            amazonS3Service.deleteFile(quizQuestion.getImage());
        } catch (java.io.IOException e) {
            throw new IOException("문제 이미지 실패!", e);
        }

        // 선택지 찾아오기
        List<QuizChoices> list = quizChoicesRepository.findAllByQuizQuestion(quizQuestion);
        // 삭제 InBatch가 기본 값으로 100개씩 삭제라고 효율이 좋다고 하네요?
        quizChoicesRepository.deleteAllInBatch(list);
        quizQuestionRepository.delete(quizQuestion);

        return ResponseEntity.ok(new MsgResponse("문제를 삭제하셨습니다. "));
    }

    //퀴즈 선택지
    public ResponseEntity<MsgResponse> deleteChoices(Long id, Member member) {

        QuizChoices quizChoices = quizChoicesRepository.findById(id).orElseThrow( ()
         -> new NullPointerException("해당 선택지는 없는 선택지입니다. "));
        if (!member.getId().equals(quizChoices.getQuizQuestion().getQuiz().getMemberId())) {
            MsgResponse msgResponse = new MsgResponse("퀴즈 생성자가 아닙니다. ");
            return ResponseEntity.badRequest().body(msgResponse);
        }
        quizChoicesRepository.delete(quizChoices);

        return ResponseEntity.ok(new MsgResponse("삭제 성공!"));
    }

    //퀴즈 찾기
    private Quiz findQuiz(Long id) {
        return quizRepository.findById(id).orElseThrow( ()
        -> new NullPointerException("해당 퀴즈는 없는 퀴즈입니다. "));
    }

    //퀴즈의 문제 번호수를  찾아서 보여줌
    private QuizQuestion findQuizQuestion (Quiz quiz,Integer questionNum) {
        return quizQuestionRepository.findByQuizAndQuestionNum(quiz, questionNum);
    }

    // 수정이라 주석 처리
//    public MsgResponse updateQuizQuestion(Long id, UpdateQuizQuestionDto updateQuizQuestionDto) {
//        QuizQuestion quizQuestion = quizQuestionRepository.findById(id).orElseThrow(()
//                -> new NullPointerException("해당 퀴즈는 없는 퀴즈 입니다. ")
//        );
//        quizQuestion.update(updateQuizQuestionDto);
//        quizQuestionRepository.save(quizQuestion);
//        MsgResponse msgResponse = new MsgResponse("문제 수정 성공! ");
//        return msgResponse;
//    }
}
