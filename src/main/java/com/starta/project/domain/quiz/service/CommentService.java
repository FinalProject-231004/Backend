package com.starta.project.domain.quiz.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.repository.MemberRepository;
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

@Service
@RequiredArgsConstructor
public class CommentService {

    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;

    public MsgResponse createComment(CreateCommentRequestDto createCommentRequestDto, Member member) {
        Quiz quiz = quizRepository.findById(createCommentRequestDto.getId()).orElseThrow( ()
        -> new NullPointerException("해당 퀴즈가 없습니다. "));
        Comment comment = new Comment();
        comment.set(quiz,createCommentRequestDto,member);
        commentRepository.save(comment);
        return new MsgResponse("댓글 작성을 성공했습니다");
    }


    @Transactional
    public ResponseEntity<MsgResponse> updateComment(Long id, UpdateCommentResponseDto updateCommentResponseDto, Member member) {
        Comment comment = findComment(id);

        if(!member.getId().equals(comment.getMember().getId()) ) {
            return ResponseEntity.badRequest().body( new MsgResponse( "댓글을 작성한 유저만 수정 가능합니다. "));
        }
        comment.update(updateCommentResponseDto.getContent());
        commentRepository.save(comment);
        return ResponseEntity.ok().body(new MsgResponse("댓글 수정을 성공했습니다. "));
    }

    public ResponseEntity<MsgResponse> deleteComment(Long id, Member member) {
        Comment comment = findComment(id);
        if(!member.getId().equals(comment.getMember().getId()) ) {
            return ResponseEntity.badRequest().body( new MsgResponse( "댓글을 작성한 유저만 삭제 가능합니다. "));
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok().body(new MsgResponse("댓글 삭제를 성공했습니다. "));
    }

    private Comment findComment (Long id) {
        return commentRepository.findById(id).orElseThrow( () ->
                new NullPointerException("해당 댓글이 없습니다. "));
    }
}
