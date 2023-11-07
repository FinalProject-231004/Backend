package com.starta.project.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starta.project.domain.member.dto.*;
import com.starta.project.domain.member.service.KakaoService;
import com.starta.project.domain.member.service.MemberService;
import com.starta.project.domain.member.util.ValidationUtil;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final KakaoService kakaoService;
    private final ValidationUtil validationUtil;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto,
                                    BindingResult bindingResult) {
        return validationUtil.checkSignupValid(bindingResult)
                .orElseGet(() -> ResponseEntity.ok(memberService.signup(requestDto)));
    }

    @Operation(summary = "카카오 로그인")
    @GetMapping("/kakao/callback")
    public ResponseEntity<MsgResponse> kakaoLogin(@RequestParam String code,
                                                  HttpServletResponse response) throws JsonProcessingException {
        return ResponseEntity.ok(kakaoService.kakaoLogin(code, response));
    }
    @Operation(summary = "카카오 신규회원 비밀번호 변경")
    @PostMapping("/kakao/first-login")
    public ResponseEntity<MsgResponse> kakaoFirstLogin(@Valid @RequestBody KaKaoFirstLoginDto requestDto,
                                                       BindingResult bindingResult,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return validationUtil.checkKakaoValid(bindingResult)
                .orElseGet(() -> ResponseEntity.ok(memberService.kakaoFirstLogin(requestDto, userDetails.getMember().getId())));
    }
    @Operation(summary = "마이페이지 내 정보 불러오기(프로필, 닉네임, 비밀번호)")
    @GetMapping("/update/view")
    public ResponseEntity<MsgDataResponse> getUserDetailView(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.getUserDetailView(userDetails.getMember()));
    }
    @Operation(summary = "profile 수정")
    @PutMapping("/update/profile")
    public ResponseEntity<MsgResponse> updateProfile(@RequestPart("newImage") MultipartFile newImage,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.updateProfile(newImage, userDetails.getMember().getId()));
    }
    @Operation(summary = "Nickname 수정")
    @PutMapping("/update/nickname")
    public ResponseEntity<MsgResponse> updateNickname(@Valid @RequestBody UpdateNicknameRequestDto requestDto,
                                                      BindingResult bindingResult,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return validationUtil.checkNicknameValid(bindingResult)
                .orElseGet(() -> ResponseEntity.ok(memberService.updateNickname(requestDto, userDetails.getMember().getId())));
    }
    @Operation(summary = "Password 수정")
    @PutMapping("/update/password")
    public ResponseEntity<MsgResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequestDto requestDto,
                                                      BindingResult bindingResult,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return validationUtil.checkPasswordValid(bindingResult)
                .orElseGet(() -> ResponseEntity.ok(memberService.updatePassword(requestDto, userDetails.getMember().getId())));
    }

    @Operation(summary = "마이페이지 내 정보 변경 비밀번호 검증(Id값 로딩)")
    @GetMapping("/validatePassword")
    public ResponseEntity<MsgDataResponse> getUserId(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(new MsgDataResponse("내 정보 변경 화면로딩 성공!", userDetails.getMember().getUsername()));
    }
    @Operation(summary = "Nickname 수정 - 중복검증")
    @PostMapping("/validate/nickname")
    public ResponseEntity<MsgResponse> validateNickname(@Valid @RequestBody UpdateNicknameRequestDto requestDto,
                                                        BindingResult bindingResult) {
        return validationUtil.checkNicknameValid(bindingResult)
                .orElseGet(() -> ResponseEntity.ok(memberService.validateNickname(requestDto)));
    }
    @Operation(summary = "마이페이지 내 정보 변경 비밀번호 검증")
    @PostMapping("/validate/password")
    public ResponseEntity<MsgResponse> validatePassword(@Valid @RequestBody PasswordValidationRequestDto requestDto,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.validatePassword(requestDto, userDetails.getMember()));
    }
    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/delete")
    public ResponseEntity<MsgResponse> deleteMember(@RequestBody PasswordValidationRequestDto requestDto,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.deleteMember(requestDto.getEnterPassword(), userDetails.getMember()));
    }
}