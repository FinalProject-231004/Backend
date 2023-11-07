package com.starta.project.domain.member.util;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValidationUtil {
    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final QuizRepository quizRepository;
    private final CommentRepository commentRepository;

    public Optional<ResponseEntity<MsgResponse>> checkSignupValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return Optional.of(ResponseEntity.badRequest().body(new MsgResponse(errorMessage)));
        }
        return Optional.empty();
    }
    public Optional<ResponseEntity<MsgResponse>> checkKakaoValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return Optional.of(ResponseEntity.badRequest().body(new MsgResponse(errorMessage)));
        }
        return Optional.empty();
    }
    public Optional<ResponseEntity<MsgResponse>> checkNicknameValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return Optional.of(ResponseEntity.badRequest().body(new MsgResponse(errorMessage)));
        }
        return Optional.empty();
    }
    public Optional<ResponseEntity<MsgResponse>> checkPasswordValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return Optional.of(ResponseEntity.badRequest().body(new MsgResponse(errorMessage)));
        }
        return Optional.empty();
    }

    public void checkDuplicatedUsername(String username){
        Optional<Member> checkUsername = memberRepository.findByUsername(username);
        if(checkUsername.isPresent()){
            throw new IllegalArgumentException("중복된 username 입니다.");
        }
    }
    public void checkDuplicatedNick(String nickname){
        Optional<MemberDetail> checkNickname = memberDetailRepository.findByNickname(nickname);
        if(checkNickname.isPresent()){
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }
    }
    public void checkPassword(String password, String checkPassword){
        if(!Objects.equals(password, checkPassword)){
            throw new IllegalArgumentException("패스워드 확인이 일치하지 않습니다.");
        }
    }
    public Member findMember(Long id){
        return memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    public MemberDetail findMemberDetail(Long id){
        return memberDetailRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    public Quiz findQuiz(Long id){
        return quizRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 퀴즈가 존재하지 않습니다."));
    }
    public Comment findComment(Long id){
        return commentRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
