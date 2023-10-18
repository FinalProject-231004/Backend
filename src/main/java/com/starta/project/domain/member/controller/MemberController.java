package com.starta.project.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starta.project.domain.member.dto.MemberUpdateRequestDto;
import com.starta.project.domain.member.dto.PasswordValidationRequestDto;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.service.KakaoService;
import com.starta.project.domain.member.service.MemberService;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final KakaoService kakaoService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<MsgResponse> signup(@Valid @RequestBody SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new MsgResponse("회원가입 실패"));
        }
        return ResponseEntity.ok(memberService.signup(requestDto));

    }

    @Operation(summary = "카카오 로그인용 서버컨트롤러")
    @GetMapping("/kakao/callback")
    public ResponseEntity<MsgResponse> kakaoLogin(@RequestParam String code,
                                                  HttpServletResponse response) throws JsonProcessingException {
        // code: 카카오 서버로부터 받은 인가 코드 Service 전달 후 인증 처리 및 JWT 반환
        // kakaoService.kakaoLogin(code, response) : 카카오 인증으로 토큰 발행 후 헤더에 저장
        return ResponseEntity.ok(kakaoService.kakaoLogin(code, response));
    }

    @Operation(summary = "Nickname 및 Password 수정")
    @PutMapping("/update")
    public ResponseEntity<MsgResponse> memberDetailUpdate(@Valid @RequestBody MemberUpdateRequestDto requestDto,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          BindingResult bindingResult){
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new MsgResponse("회원정보 수정 실패"));
        }
        return ResponseEntity.status(200).body(memberService.updateMemberDetail(requestDto, userDetails.getMember().getId()));

    }

    @Operation(summary = "마이페이지 정보수정용 비밀번호 검증 API(validate Password)")
    @PostMapping("/validatePassword")
    public ResponseEntity<MsgResponse> validatePassword(@Valid @RequestBody PasswordValidationRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.validatePassword(requestDto, userDetails.getMember()));
    }

}