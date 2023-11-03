package com.starta.project.domain.quiz.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.entity.NotificationType;
import com.starta.project.domain.notification.service.NotificationService;
import com.starta.project.domain.quiz.dto.CreateQuizRequestDto;
import com.starta.project.domain.quiz.dto.CreateQuizResponseDto;
import com.starta.project.domain.quiz.dto.ShowQuizResponseDto;
import com.starta.project.domain.quiz.entity.*;
import com.starta.project.domain.quiz.repository.*;
import com.starta.project.global.aws.AmazonS3Service;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoicesRepository quizChoicesRepository;
    private final LikesRepository likesRepository;
    private final AmazonS3Service amazonS3Service;
    private final MemberDetailRepository memberDetailRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;
    private final MileageGetHistoryRepository getHistoryRepository;

    //퀴즈 만들기
    @Transactional
    public ResponseEntity<MsgDataResponse> createQuiz(MultipartFile multipartFile, CreateQuizRequestDto quizRequestDto,
                                                      Member member) {
        LocalDateTime localDate = LocalDateTime.now();

        Optional<MileageGetHistory> getHistory = getHistoryRepository.findFirstByMemberDetailAndTypeOrderByDateDesc(
                 member.getMemberDetail(), TypeEnum.QUIZ_CREATE);

        if(getHistory.isEmpty() || getHistory.get().getDate().isEqual(localDate)){
            MemberDetail memberDetail = member.getMemberDetail();
            Integer i = 50;
            memberDetail.gainMileagePoint(i);
            memberDetailRepository.save(memberDetail);
            MileageGetHistory mileageGetHistory = new MileageGetHistory();
            String des = "오늘의 퀴즈 생성";
            mileageGetHistory.getFromQuiz(memberDetail,i,des);
            getHistoryRepository.save(mileageGetHistory);
        }

        Quiz quiz = new Quiz();
        String image;
        //이미지
        try {
             image = amazonS3Service.upload(multipartFile);
        } catch (java.io.IOException e) {
            throw new IOException("이미지 업로드에 문제가 실패",e);
        }

        //유저네임
        String nickname = member.getMemberDetail().getNickname();
        Long memberId = member.getId();
        //생성시간
        LocalDateTime now = LocalDateTime.now();
        //퀴즈 생성
        quiz.set(quizRequestDto,image, now, memberId,nickname);
        quizRepository.save(quiz);
        //퀴즈 반환
        CreateQuizResponseDto quizResponseDto = new CreateQuizResponseDto();
        quizResponseDto.set(quiz.getId());
        MsgDataResponse msgDataResponse = new MsgDataResponse("퀴즈 생성 성공!" , quizResponseDto);
        return ResponseEntity.ok().body(msgDataResponse);
    }

    // 문제 상세 보기
    public ResponseEntity<ShowQuizResponseDto> showQuiz(Long id) {
            ShowQuizResponseDto showQuizResponseDto = new ShowQuizResponseDto();
            Quiz quiz = findQuiz(id);
            if(quiz.getDisplay()== false )  {
                throw new IllegalArgumentException("게시된 퀴즈가 아닙니다. ");
            }
            //조회수 => api 검색 = 조회하는 횟수 -> 이거 조회 api 안해도 될꺼 같은데..?
            // 만약 할꺼면 여기다 동시성 제어를 걸어야 할거 같습니다!
            Integer viewCount = quiz.getViewCount();
            viewCount++;
            quiz.view(viewCount);
            quizRepository.save(quiz);
            //반환하는 데이터
            showQuizResponseDto.set(quiz,viewCount);

            return ResponseEntity.status(200).body(showQuizResponseDto);
    }

    @Transactional //퀴즈 삭제
    public ResponseEntity<MsgResponse> deleteQuiz(Long id, Member member) {
        //이전의 것과 마찬가지 입니다.
        Quiz quiz = findQuiz(id);
//        //유저 확인
//        if (!member.getId().equals(quiz.getMemberId())) {
//            MsgResponse msgResponse = new MsgResponse("퀴즈 생성자가 아닙니다. ");
//            return ResponseEntity.badRequest().body(msgResponse);
//        }

        //하위 항목 + 이미지 찾아서 리스트 만들기
        List<Likes> likes = likesRepository.findAllByQuiz(quiz);
        List<Comment> comments = getComment(id);
        List<QuizQuestion> quizQuestionList = quizQuestionRepository.findAllByQuiz(quiz);
        List<QuizChoices> quizChoicesList = new ArrayList<>();
        List<String> imageList = new ArrayList<>();
        for (QuizQuestion quizQuestion : quizQuestionList) {
            imageList.add(quizQuestion.getImage());
            List<QuizChoices> quizChoices = quizChoicesRepository.findAllByQuizQuestion(quizQuestion);
            quizChoicesList.addAll(quizChoices);
        }
        //이미지 삭제
        imageList.add(quiz.getImage());
        for (String image : imageList) {
            try {
                amazonS3Service.deleteFile(image) ;
            } catch (java.io.IOException e) {
                throw new IOException("이미지 삭제 실패 ",e);
            }
        }
        // 여기도 마찬가지로 효율이 좋다고하네요? (테스트 결과 문제수 22개, 문항 수 44개 before 1199ms | after 139 ms)
        likesRepository.deleteAllInBatch(likes);
        commentRepository.deleteAllInBatch(comments);
        quizChoicesRepository.deleteAllInBatch(quizChoicesList);
        quizQuestionRepository.deleteAllInBatch(quizQuestionList);
        quizRepository.delete(quiz);

        return ResponseEntity.ok(new MsgResponse("퀴즈 삭제 성공! "));
    }


    @Transactional //좋아요
    public MsgResponse pushLikes(Long id, Member member) {

        Quiz quiz = findQuiz(id);
        Integer likesNum = quiz.getLikes();
        Optional<Likes> likesOptional = likesRepository.findByMemberIdAndQuiz(member.getId(),quiz);

        if (likesOptional.isPresent()){
            likesNum--;
            quiz.pushLikes(likesNum);
            likesRepository.delete(likesOptional.get());
            return new MsgResponse("좋아요를 취소했습니다! ");
        }

        Likes likes = new Likes();
        likes.set(quiz,member);
        likesRepository.save(likes);
        likesNum++;
        quiz.pushLikes(likesNum);

        //알림
        Optional<Member> memberOptional =  memberRepository.findById(quiz.getMemberId());
        if(memberOptional.isPresent()) {
            String sender = member.getUsername();
            String receiver = memberOptional.get().getUsername();
            String notificationId = receiver + "_" + System.currentTimeMillis();
            String title = quiz.getTitle();
            String content = "";
            if(title.length() < 4) {
                content = "["
                        + title
                        + "]"
                        + "게시글 좋아요가 추가되었습니다. ";
            } else {
                content = "["
                        + title.substring(0, 3) + "..."
                        + "]"
                        + "게시글 좋아요가 추가되었습니다. ";
            }

            String type = NotificationType.LIKEQUIZ.getAlias();

            Notification notification = Notification.builder()
                    .notificationId(notificationId)
                    .receiver(receiver)
                    .content(content)
                    .notificationType(type)
                    .url("/api/quiz/" + quiz.getId())
                    .readYn('N')
                    .deletedYn('N')
                    .created_at(LocalDateTime.now())
                    .build();
            //작성자 본인이 댓글/대댓글을 단 것이 아닌 경우에 한하여 알림
            if(!receiver.equals(sender)) notificationService.sendNotification(notification);
        }
        return new MsgResponse("좋아요를 눌렀습니다. ");
    }

    //게시하기
    public ResponseEntity<MsgResponse> display(Long id, Long memberId) {
        Quiz quiz = findQuiz(id);
        if(!quiz.getMemberId().equals(memberId)) {
            return ResponseEntity.badRequest().body(new MsgResponse("작성자만 게시 가능합니다. "));
        }
        quiz.play(true);
        quizRepository.save(quiz);
        return ResponseEntity.ok(new MsgResponse("퀴즈를 게시합니다. "));
    }


    //퀴즈 찾기
    private Quiz findQuiz (Long id) {
       return quizRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 퀴즈가 없습니다."));
    }

    //댓글 찾기
    private List<Comment> getComment(Long id) {
        List<Comment> commentList = commentRepository.findAllByQuizId(id);
        return commentList;
    }


    //수정이기 때문에 주석 처리
//    public MsgResponse update(Long id, CreateQuizRequestDto quizRequestDto) {
//        Quiz quiz = findQuiz(id);
//        quiz.update(quizRequestDto);
//        quizRepository.save(quiz);
//        MsgResponse msgResponse = new MsgResponse("문제를 수정하셨습니다.");
//        return msgResponse;
//    }

}
