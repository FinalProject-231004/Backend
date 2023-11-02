package com.starta.project.domain.member.util;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ValidationUtil {
    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;

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
    public Member findMember(Long id){
        return memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }
    public void checkPassword(String password, String checkPassword){
        if(!Objects.equals(password, checkPassword)){
            throw new IllegalArgumentException("패스워드 변경이 일치하지 않습니다.");
        }
    }
}
