package com.starta.project.domain.member.service;

import com.starta.project.domain.member.dto.PasswordValidationRequestDto;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.dto.UpdateNicknameRequestDto;
import com.starta.project.domain.member.dto.UpdatePasswordRequestDto;
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
    public MsgResponse updateNickname(UpdateNicknameRequestDto requestDto, Long id) {
        Member member = findMember(id);
        MemberDetail memberDetail = member.getMemberDetail();
        if (memberDetailRepository.findByNickname(requestDto.getNewNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        memberDetail.updateNickname(requestDto.getNewNickname());
        return new MsgResponse("닉네임 변경완료.");


    }
    @Transactional
    public MsgResponse updatePassword(UpdatePasswordRequestDto requestDto, Long id) {

        Member member = findMember(id);
        MemberDetail memberDetail = member.getMemberDetail();
        String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        member.updatePassword(encodedPassword);
        return new MsgResponse("비밀번호 변경완료");
    }

    @Transactional  // 일관성 유지를 위해 사용
    public MsgResponse deleteMember(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        memberRepository.delete(member);
        return new MsgResponse("탈퇴완료.");
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );
    }

}
