package com.starta.project.domain.member.service;

import com.starta.project.domain.member.dto.LoginRequestDto;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.member.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 *  @vaild 를 통과하지 못한 경우 Controller에서 처리되기 때문에 ServiceTest에서는 정상값만 체크함.
 */

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    MemberDetailRepository memberDetailRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    ValidationUtil validationUtil;
    @InjectMocks
    MemberService memberService;


    @Nested
    @DisplayName("SignUp")
    class SignUpUser {

        @Test
        @Transactional
        @DisplayName("회원가입 성공")
        void signupUser_success() {

            SignupRequestDto signupRequestDto = new SignupRequestDto("test1",
                    "test1",
                    "test123!",
                    "test123!",
                    false);

            // Given
            String username = signupRequestDto.getUsername();
            String nickname = signupRequestDto.getNickname();
            String password = signupRequestDto.getPassword();
            String checkPassword = signupRequestDto.getCheckPassword();
            UserRoleEnum role = UserRoleEnum.USER;
            if(signupRequestDto.isAdmin()){
                role = UserRoleEnum.ADMIN;
            }
                ;
            Member member = new Member(username, password , role);
            MemberDetail memberDetail = new MemberDetail(nickname);

            when(passwordEncoder.encode(signupRequestDto.getPassword())).thenReturn("encodedPassword");
            doNothing().when(validationUtil).checkDuplicatedUsername(username);
            doNothing().when(validationUtil).checkDuplicatedNick(nickname);
            doNothing().when(validationUtil).checkPassword(password, checkPassword);

            when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
                Member savedMember = invocation.getArgument(0);
                savedMember.setId(1L); // 가정된 ID 설정
                return savedMember;
            });

            when(memberDetailRepository.save(any(MemberDetail.class))).thenAnswer(invocation -> {
                MemberDetail savedDetail = invocation.getArgument(0);
                savedDetail.setMember(member); // 가정된 Member 연결
                savedDetail.setId(1L); // 가정된 ID 설정
                return savedDetail;
            });

            // When
            memberService.signup(signupRequestDto);

            // Then
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(1)).save(memberCaptor.capture());
            Member savedMember = memberCaptor.getValue();
            assertEquals(signupRequestDto.getUsername(), savedMember.getUsername());
            assertEquals("encodedPassword", savedMember.getPassword());

            ArgumentCaptor<MemberDetail> memberDetailCaptor = ArgumentCaptor.forClass(MemberDetail.class);
            verify(memberDetailRepository, times(1)).save(memberDetailCaptor.capture());
            MemberDetail savedMemberDetail = memberDetailCaptor.getValue();
            assertEquals(signupRequestDto.getNickname(), savedMemberDetail.getNickname());
        }
    }
}
