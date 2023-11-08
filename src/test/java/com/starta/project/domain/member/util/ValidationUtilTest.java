package com.starta.project.domain.member.util;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
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

    @InjectMocks
    private ValidationUtil validationUtil;

    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        bindingResult = new BeanPropertyBindingResult(null, "");
    }

    @Test
    @DisplayName("Username 중복체크")
    void checkDuplicatedUsername_ThrowsExceptionIfUsernameExists() {
        // Given
        String username = "testUser";
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(new Member()));

        // When / Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationUtil.checkDuplicatedUsername(username)
        );
        assertEquals("중복된 username 입니다.", exception.getMessage());
    }


}
