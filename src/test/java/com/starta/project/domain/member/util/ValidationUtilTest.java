package com.starta.project.domain.member.util;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberDetailRepository memberDetailRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ValidationUtil validationUtil;

    private BindingResult bindingResult;
    private Member member;
    private MemberDetail memberDetail;
    private Quiz quiz;
    private Comment comment;



    @BeforeEach
    void setUp() {
        member = new Member("username", "password1!", UserRoleEnum.USER);
        memberDetail = new MemberDetail("nick1");
        quiz = new Quiz();
        comment = new Comment();

        bindingResult = new BeanPropertyBindingResult(member, "member");
    }

    @Test
    @DisplayName("1. 유효성 검사 확인 - 오류발생시 등록한 상태코드 반환 검증")
    void signup_exception(){
        // bindingResult에러 직접 발생
        bindingResult.rejectValue("username", "error.username", "Username is required");
        Optional<ResponseEntity<MsgResponse>> result = validationUtil.checkSignupValid(bindingResult);
        assertTrue(result.isPresent());  // 오류 메세지가 있을 경우
        assertEquals(400, result.get().getStatusCodeValue());  // 400에러 발생하는지 확인
    }

    @Test
    @DisplayName("2. Username 중복체크 - 중복에 맞게 값 반환")
    void checkDuplicatedUsername() {
        // Given
        String username = "testuser";
        when(memberRepository.findByUsername(username))
                .thenReturn(Optional.empty());
        // When / Then
        assertDoesNotThrow(() -> validationUtil.checkDuplicatedUsername(username));
    }

    @Test
    @DisplayName("3. Username 중복체크 - 중복예외 발생")
    void checkDuplicatedUsername_ThrowsException() {
        // Given
        String username = "testuser";
        when(memberRepository.findByUsername(username))
                .thenReturn(Optional.of(new Member()));

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationUtil.checkDuplicatedUsername(username)
        );
        assertEquals("중복된 username 입니다.", exception.getMessage());
    }


    @Test
    @DisplayName("4. 닉네임 중복 검사 - 중복예외 발생")
    void checkDuplicatedNick_ThrowsExceptionIfNicknameExists() {
        // Given
        String nickname = "nickname";
        when(memberDetailRepository.findByNickname(nickname))
                .thenReturn(Optional.of(new MemberDetail()));

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationUtil.checkDuplicatedNick(nickname)
        );
        assertEquals("중복된 nickname 입니다.", exception.getMessage());
    }
    @Test
    @DisplayName("5. 비밀번호 확인 - 불일치로 예외발생")
    void checkPassword_ThrowsExceptionIfPasswordsDoNotMatch() {
        // Given
        String password = "password";
        String checkPassword = "differentPassword";

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationUtil.checkPassword(password, checkPassword)
        );
        assertEquals("패스워드 확인이 일치하지 않습니다.", exception.getMessage());
    }

}
