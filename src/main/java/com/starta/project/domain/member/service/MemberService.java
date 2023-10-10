package com.starta.project.domain.member.service;

import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final PasswordEncoder passwordEncoder;
    public MsgResponse signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String nickname = requestDto.getNickname();
        String password = passwordEncoder.encode(requestDto.getPassword());

        if(!Objects.equals(requestDto.getCheckpassword(), requestDto.getPassword())){
            throw new IllegalArgumentException("패스워드와 패스워드 확인이 다릅니다.");
        }
        // username 중복 확인
        Optional<Member> checkUsername = memberRepository.findByUsername(username);
        if(checkUsername.isPresent()){
            throw new IllegalArgumentException("중복된 username 입니다.");
        }

        // nickname 중복 확인
        Optional<MemberDetail> checkNickname = memberDetailRepository.findByNickname(nickname);
        if(checkNickname.isPresent()){
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }

        // 사용자 ROLE 확인 (기본값: USER)
        UserRoleEnum role = UserRoleEnum.USER;
        if(requestDto.isAdmin()){
            role = UserRoleEnum.ADMIN;
        }

        // 회원 정보 저장
        Member savedMember = memberRepository.save(new Member(username, password, role));

        // 회원 상세 정보 저장 및 연관 관계 설정
        MemberDetail memberDetail = new MemberDetail(nickname);
        memberDetail.setMember(savedMember);
        memberDetailRepository.save(memberDetail);

        return new MsgResponse("회원가입 성공");
    }
}
