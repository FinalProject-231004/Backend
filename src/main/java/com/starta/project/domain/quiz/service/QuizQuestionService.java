package com.starta.project.domain.quiz.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.quiz.dto.CreateQuestionRequestDto;
import com.starta.project.domain.quiz.dto.CreateQuizChoicesDto;
import com.starta.project.domain.quiz.dto.ShowQuestionResponseDto;
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

    //퀴즈 생성
    @Transactional
    public ResponseEntity<MsgResponse> createQuizQuestion(Long id, Optional<MultipartFile> multipartFile,
                                                          CreateQuestionRequestDto createQuestionRequestDto,
                                                          Member member) {
        //퀴즈 찾기
        Quiz quiz = findQuiz(id);
        //퀴즈 생성자 확인
        if (!member.getId().equals(quiz.getMemberId())) {
            MsgResponse msgResponse = new MsgResponse("퀴즈 생성자가 아닙니다. ");
            return ResponseEntity.badRequest().body(msgResponse);
        }
        //이미지 추가
        String image = "";
        //이미지
        if(multipartFile.isPresent()){
            try {
                 image = amazonS3Service.upload(multipartFile.get());
            } catch (java.io.IOException e) {
                throw new IOException("이미지 업로드에 문제가 있습니다!  ");
            }
        }
        //문제 번호 찾기
        Integer questionNum = 0;
        //findTop == 가장 먼저 찾을 수 있는 항목(살짝 다르긴 하지만 가장 큰 값이라는 것은 있다.)
        Optional<QuizQuestion> question =  quizQuestionRepository.findTopByQuizOrderByQuestionNumDesc(quiz);
        if (question.isPresent()){
            questionNum = question.get().getQuestionNum();
        }
        //문제 만들기
        QuizQuestion quizQuestion = new QuizQuestion();
        questionNum++;
        quizQuestion.set(quiz,questionNum , createQuestionRequestDto.getTitle(), image);
        quizQuestionRepository.save(quizQuestion);
        //선택지 만들기 [] 형식
        List<CreateQuizChoicesDto> quizChoicesList = createQuestionRequestDto.getQuizChoices();
        List<QuizChoices> quizChoices = new ArrayList<>();
        for (CreateQuizChoicesDto createQuizChoicesDto : quizChoicesList) {
            QuizChoices quizChoices1 = new QuizChoices();
            quizChoices1.set(createQuizChoicesDto,quizQuestion);
            quizChoices.add(quizChoices1);
        }
        quizChoicesRepository.saveAll(quizChoices);
        return ResponseEntity.ok(new MsgResponse("문제 생성을 성공 하셨습니다!"));
    }

    //문항 한 문제씩 보기
    public ShowQuestionResponseDto showQuizQuestion(Long id, Integer questionNum) {
        // 퀴즈 찾기
        Quiz quiz = findQuiz(id);
        // 해당 퀴즈의 n번 문제 찾기
        QuizQuestion quizQuestion = findQuizQuestion(quiz, questionNum);
        // 선택지 찾아오기
        List<QuizChoices> list = quizChoicesRepository.findAllByQuizQuestion(quizQuestion);
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
