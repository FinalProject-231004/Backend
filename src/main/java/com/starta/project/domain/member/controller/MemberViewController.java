package com.starta.project.domain.member.controller;

import com.starta.project.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberViewController {

    private MemberService memberService;

    @Operation(summary = "(view) 로그인")
    @GetMapping("/member/login-page")
    public String loginPage() {
        return "login";
    }

    @Operation(summary = "임시테스트용 API(삭제예정)")
    @GetMapping("/member/signup")
    public String signupPage() {
        return "signup";
    }

    @Operation(summary = "임시테스트용 API(삭제예정)")
    @GetMapping("/test")
    @ResponseBody
    public void test(){
        System.out.println("TEST 성공");
    }

}
