package com.starta.project.domain.mypage.controller;

import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import com.starta.project.domain.mypage.service.MyPageService;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마일리지 구매내역")
    @GetMapping("/mypage/purchase-history")
    public ResponseEntity<MsgDataResponse> getItemsByCategory(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(myPageService.getPurchaseHistory(userDetails.getMember()));
    }

}
