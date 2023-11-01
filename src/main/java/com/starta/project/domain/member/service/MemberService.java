package com.starta.project.domain.member.service;

import com.starta.project.domain.member.dto.*;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.member.util.ValidationUtil;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import com.starta.project.global.aws.AmazonS3Service;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final AmazonS3Service amazonS3Service;
    private final ValidationUtil validationUtil;

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
        // 회원 정보 저장
        Member savedMember = memberRepository.save(new Member(username, password, role));

        // 회원 상세 정보 저장 및 연관 관계 설정
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
            // 기존 이미지가 없는 경우
            if (oldImageUrl == null) {
                String imageUrl = amazonS3Service.upload(newImage); // S3에 새 이미지 업로드
                memberDetail.updateImage(imageUrl); // 이미지 URL을 업데이트
            } else {
                // 기존 이미지가 있는 경우
                amazonS3Service.deleteFile(oldImageUrl.split("/")[3]); // S3 기존 이미지 삭제
                String imageUrl = amazonS3Service.upload(newImage); // S3에 새 이미지 업로드
                memberDetail.updateImage(imageUrl); // 이미지 URL을 업데이트
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
        validationUtil.checkPassword(newPassword, requestDto.getNewCheckPassword());
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
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

    @Transactional  // 일관성 유지를 위해 사용
    public MsgResponse deleteMember(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        memberRepository.delete(member);
        return new MsgResponse("탈퇴완료.");
    }


}
