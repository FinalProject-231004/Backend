package com.starta.project.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starta.project.domain.member.dto.PasswordValidationRequestDto;
import com.starta.project.domain.member.dto.SignupRequestDto;
import com.starta.project.domain.member.dto.UpdateNicknameRequestDto;
import com.starta.project.domain.member.dto.UpdatePasswordRequestDto;
import com.starta.project.domain.member.service.KakaoService;
import com.starta.project.domain.member.service.MemberService;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    public ResponseEntity<MsgResponse> signup(@Valid @RequestBody SignupRequestDto requestDto,
                                              BindingResult bindingResult) {
        // Validation 예외처리
//        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//        if (!fieldErrors.isEmpty()) {
//            for (FieldError fieldError : bindingResult.getFieldErrors()) {
//                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
//            }
//            return ResponseEntity.badRequest().body(new MsgResponse("회원가입 실패"));
//        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new MsgResponse("회원가입 실패"));
        }
        return ResponseEntity.ok(memberService.signup(requestDto));
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao/callback")
    public ResponseEntity<MsgResponse> kakaoLogin(@RequestParam String code,
                                                  HttpServletResponse response) throws JsonProcessingException {
        return ResponseEntity.ok(kakaoService.kakaoLogin(code, response));
    }
    @Operation(summary = "Nickname 수정")
    @PutMapping("/update/nickname")
    public ResponseEntity<MsgResponse> updateNickname(@Valid @RequestBody UpdateNicknameRequestDto requestDto,
                                                      BindingResult bindingResult,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Validation 예외처리
//        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//        if (!fieldErrors.isEmpty()) {
//            for (FieldError fieldError : bindingResult.getFieldErrors()) {
//                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
//            }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new MsgResponse("닉네임 수정 실패"));
        }
        return ResponseEntity.ok(memberService.updateNickname(requestDto, userDetails.getMember().getId()));
    }

    @Operation(summary = "Password 수정")
    @PutMapping("/update/password")
    public ResponseEntity<MsgResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequestDto requestDto,
                                                      BindingResult bindingResult,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Validation 예외처리
//        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//        if (!fieldErrors.isEmpty()) {
//            for (FieldError fieldError : bindingResult.getFieldErrors()) {
//                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
//            }
//            return ResponseEntity.badRequest().body(new MsgResponse("비밀번호 수정 실패"));
//        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new MsgResponse("비밀번호 수정 실패"));
        }
        return ResponseEntity.ok(memberService.updatePassword(requestDto, userDetails.getMember().getId()));
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/delete")
    public ResponseEntity<MsgResponse> deleteMember(@RequestBody PasswordValidationRequestDto requestDto,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(200).body(memberService.deleteMember(requestDto.getEnterPassword(), userDetails.getMember()));
    }


    @Operation(summary = "마이페이지 내 정보 변경")
    @GetMapping("/validatePassword")
    public ResponseEntity<MsgDataResponse> getUserId(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(new MsgDataResponse("내 정보 변경 화면로딩 성공!", userDetails.getMember().getUsername()));
    }

    @Operation(summary = "마이페이지 내 정보 변경 비밀번호 검증")
    @PostMapping("/validatePassword")
    public ResponseEntity<MsgResponse> validatePassword(@Valid @RequestBody PasswordValidationRequestDto requestDto,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.validatePassword(requestDto, userDetails.getMember()));
    }
}