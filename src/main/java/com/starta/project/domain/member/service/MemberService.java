package com.starta.project.domain.member.service;

import com.starta.project.domain.answer.repository.MemberAnswerRepository;
import com.starta.project.domain.member.dto.*;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.member.util.ValidationUtil;
import com.starta.project.domain.mypage.repository.AttendanceCheckRepository;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.global.aws.AmazonS3Service;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;
    private final MemberAnswerRepository memberAnswerRepository;
    private final AttendanceCheckRepository attendanceCheckRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmazonS3Service amazonS3Service;
    private final ValidationUtil validationUtil;

    @Transactional
    public MsgResponse signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String nickname = requestDto.getNickname();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원가입 정보 검증
        validationUtil.checkDuplicatedUsername(username);
        validationUtil.checkDuplicatedNick(nickname);
        validationUtil.checkPassword(requestDto.getPassword(), requestDto.getCheckPassword());

        // 사용자 ROLE 확인 (기본값: USER)
        UserRoleEnum role = UserRoleEnum.USER;
        if(requestDto.isAdmin()){
            role = UserRoleEnum.ADMIN;
        }
        Member savedMember = memberRepository.save(new Member(username, password, role));

        MemberDetail memberDetail = new MemberDetail(nickname);
        memberDetail.setMember(savedMember);
        memberDetailRepository.save(memberDetail);

        return new MsgResponse("회원가입 성공");
    }


    public MsgDataResponse getUserDetailView(Member member) {
        String image = member.getMemberDetail().getImage();
        String nickname = member.getMemberDetail().getNickname();

        return new MsgDataResponse("내 정보 불러오기 성공!", new MemberViewResponseDto(image,nickname));
    }

    @Transactional
    public MsgResponse updateProfile(MultipartFile newImage, Long memberId) {
        MemberDetail memberDetail = memberDetailRepository.findByMemberId(memberId);
        String oldImageUrl = memberDetail.getImage();
        System.out.println("S3 oldImage: " + oldImageUrl);
        try {
            if (oldImageUrl == null) {
                String imageUrl = amazonS3Service.upload(newImage);
                memberDetail.updateImage(imageUrl);
            } else {
                amazonS3Service.deleteFile(oldImageUrl.split("/")[3]);
                String imageUrl = amazonS3Service.upload(newImage);
                memberDetail.updateImage(imageUrl);
            }
        } catch (IOException e) {
            return new MsgResponse("이미지 업로드 또는 삭제 중에 오류가 발생했습니다.");
        }
        return new MsgResponse("프로필 이미지 업데이트 완료.");
    }


    @Transactional
    public MsgResponse updateNickname(UpdateNicknameRequestDto requestDto, Long id) {
        Member member = validationUtil.findMember(id);
        MemberDetail memberDetail = member.getMemberDetail();

        validationUtil.checkDuplicatedNick(requestDto.getNewNickname());

        memberDetail.updateNickname(requestDto.getNewNickname());
        return new MsgResponse("닉네임 변경완료.");
    }
    @Transactional
    public MsgResponse kakaoFirstLogin(KaKaoFirstLoginDto requestDto, Long id) {
        Member member = validationUtil.findMember(id);
        String newPassword = requestDto.getNewPassword();
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        validationUtil.checkPassword(newPassword, requestDto.getNewCheckPassword());
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.updatePassword(encodedPassword);
        return new MsgResponse("카카오 신규유저 비밀번호 변경완료");
    }

    @Transactional
    public MsgResponse updatePassword(UpdatePasswordRequestDto requestDto, Long id) {
        Member member = validationUtil.findMember(id);
        validationUtil.checkPassword(requestDto.getNewPassword(), requestDto.getNewCheckPassword());
        String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        member.updatePassword(encodedPassword);
        return new MsgResponse("비밀번호 변경완료");
    }

    public MsgResponse validateNickname(UpdateNicknameRequestDto requestDto) {
        if (memberDetailRepository.findByNickname(requestDto.getNewNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다!");
        }
        return new MsgResponse("사용 가능한 닉네임입니다!");
    }
    public MsgResponse validatePassword(PasswordValidationRequestDto requestDto, Member member) {
        if (!passwordEncoder.matches(requestDto.getEnterPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new MsgResponse("비밀번호 검증 성공");
    }

    @Transactional
    public MsgResponse deleteMember(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        MemberDetail memberDetail = member.getMemberDetail();
        if (memberDetail != null) {
            mileageGetHistoryRepository.deleteAllByMemberDetail(memberDetail);
//            purchaseHistoryRepository.deleteAllByMemberDetail(memberDetail);
            memberAnswerRepository.deleteAllByMemberDetail(memberDetail);
        }
        attendanceCheckRepository.deleteAllByMember(member);
        memberRepository.delete(member);

        return new MsgResponse("탈퇴완료.");
    }

    public MsgResponse checkAdmin(Member member) {
        if (member.getRole() == UserRoleEnum.ADMIN) {
            return new MsgResponse(UserRoleEnum.ADMIN.toString());
        }
        return new MsgResponse(UserRoleEnum.USER.toString());
    }
}
