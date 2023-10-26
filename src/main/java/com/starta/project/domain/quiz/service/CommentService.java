package com.starta.project.domain.quiz.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.notification.entity.Notification;
import com.starta.project.domain.notification.entity.NotificationType;
import com.starta.project.domain.notification.service.NotificationService;
import com.starta.project.domain.quiz.dto.CreateCommentRequestDto;
import com.starta.project.domain.quiz.dto.UpdateCommentResponseDto;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    //댓글 생성
    public MsgResponse createComment(Long id, CreateCommentRequestDto createCommentRequestDto, Member member) {
        Quiz quiz = quizRepository.findById(id).orElseThrow( ()
        -> new NullPointerException("해당 퀴즈가 없습니다. "));
        Comment comment = new Comment();
        comment.set(quiz,createCommentRequestDto,member);
        commentRepository.save(comment);

        //알림
        Optional<Member> memberOptional = memberRepository.findById(quiz.getMemberId());

        if (memberOptional.isPresent()) {
            String sender = member.getUsername();
            String receiver = memberOptional.get().getUsername();
            String notificationId = receiver + "_" + System.currentTimeMillis();
            String title = quiz.getTitle();
            String content = "["
                    + title.substring(0, 3) + "..."
                    + "]"
                    + "게시글에 댓글이 달렸습니다: "
                    + "["
                    + comment.getComment().substring(0, 3) + "..."
                    + "]";
            String type = NotificationType.COMMENT.getAlias();

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
            if (!receiver.equals(sender)) notificationService.sendNotification(notification);
        }

        return new MsgResponse("댓글 작성을 성공했습니다");
    }

    //댓글 수정
    @Transactional
    public ResponseEntity<MsgResponse> updateComment(Long id, UpdateCommentResponseDto updateCommentResponseDto, Member member) {
        Comment comment = findComment(id);

        if(!member.getId().equals(comment.getMemberId()) ) {
            return ResponseEntity.badRequest().body( new MsgResponse( "댓글을 작성한 유저만 수정 가능합니다. "));
        }
        comment.update(updateCommentResponseDto.getContent());
        commentRepository.save(comment);
        return ResponseEntity.ok().body(new MsgResponse("댓글 수정을 성공했습니다. "));
    }

    //댓글 삭제
    public ResponseEntity<MsgResponse> deleteComment(Long id, Member member) {
        Comment comment = findComment(id);
        if(!member.getId().equals(comment.getMemberId()) ) {
            return ResponseEntity.badRequest().body( new MsgResponse( "댓글을 작성한 유저만 삭제 가능합니다. "));
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok().body(new MsgResponse("댓글 삭제를 성공했습니다. "));
    }

    //해당 댓글 찾기
    private Comment findComment (Long id) {
        return commentRepository.findById(id).orElseThrow( () ->
                new NullPointerException("해당 댓글이 없습니다. "));
    }
}
