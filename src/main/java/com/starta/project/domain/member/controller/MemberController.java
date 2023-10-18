package com.starta.project.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.dto.TokenResponseDto;
import com.starta.project.domain.member.service.KakaoService;
import com.starta.project.domain.member.service.MemberService;
import com.starta.project.global.jwt.JwtUtil;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    private final MemberService userService;
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
        return ResponseEntity.ok(userService.signup(requestDto));
    }
    @Operation(summary = "카카오 로그인용 서버컨트롤러")
    @GetMapping("/kakao/callback")
    public ResponseEntity<MsgResponse> kakaoLogin(@RequestParam String code,
                                                  HttpServletResponse response) throws JsonProcessingException {
        // code: 카카오 서버로부터 받은 인가 코드 Service 전달 후 인증 처리 및 JWT 반환
        // kakaoService.kakaoLogin(code, response) : 카카오 인증으로 토큰 발행 후 헤더에 저장
        return ResponseEntity.ok(kakaoService.kakaoLogin(code, response));

    }
}
