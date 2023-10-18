package com.starta.project.domain.member.service;

import com.starta.project.domain.member.dto.MemberUpdateRequestDto;
import com.starta.project.domain.member.dto.PasswordValidationRequestDto;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    public MsgResponse validatePassword(PasswordValidationRequestDto requestDto, Member member) {
        if (!passwordEncoder.matches(requestDto.getEnterPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new MsgResponse("비밀번호 검증 성공");
    }

    @Transactional
    public MsgResponse updateMemberDetail(MemberUpdateRequestDto requestDto, Long id) {

        Member findMember = memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("없는 회원입니다.")
        );
        MemberDetail memberDetail = findMember.getMemberDetail();

        if (requestDto.getNewNickname() != null && !requestDto.getNewNickname().isEmpty()) {
            Optional<MemberDetail> newNickname = memberDetailRepository.findByNickname(requestDto.getNewNickname());
            if (newNickname.isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            memberDetail.updateNickname(requestDto.getNewNickname());
        }

        if (!requestDto.getNewPassword().isEmpty()) {
            findMember.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
            System.out.println(findMember.getPassword());
        }

        return new MsgResponse("회원정보 변경완료");
    }
}
