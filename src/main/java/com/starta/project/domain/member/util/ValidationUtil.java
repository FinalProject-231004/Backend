package com.starta.project.domain.member.util;

import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValidationUtil {
    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;


    public Optional<ResponseEntity<MsgResponse>> checkSignupValid(@Valid @RequestBody SignupRequestDto requestDto,
                                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return Optional.of(ResponseEntity.badRequest().body(new MsgResponse(errorMessage)));
        }
        return Optional.empty();
    }


    public void checkDuplicatedUsername(String username){
        Optional<Member> checkUsername = memberRepository.findByUsername(username);
        if(checkUsername.isPresent()){
            throw new IllegalArgumentException("중복된 ID입니다.");
        }
    }
    public void checkDuplicatedNick(String nickname){
        Optional<MemberDetail> checkNickname = memberDetailRepository.findByNickname(nickname);
        if(checkNickname.isPresent()){
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }
    }
    public Member findMember(Long id){
        return memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    public void checkPassword(String password, String checkPassword){
        if(!Objects.equals(password, checkPassword)){
            throw new IllegalArgumentException("패스워드 확인이 일치하지 않습니다.");
        }
    }
}
