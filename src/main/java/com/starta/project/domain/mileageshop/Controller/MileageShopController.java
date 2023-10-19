package com.starta.project.domain.mileageshop.Controller;

import com.starta.project.domain.mileageshop.dto.CreateMileageItemRequestDto;
import com.starta.project.domain.mileageshop.dto.OrderItemRequestDto;
import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import com.starta.project.domain.mileageshop.service.MileageShopService;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MileageShopController {

    private final MileageShopService mileageShopService;

    @Operation(summary = "마일리지샵 구매")
    @PostMapping("/mileageshop/{mileageItemId}")
    public ResponseEntity<MsgResponse> orderItem(@PathVariable Long mileageItemId,
                                                 @RequestBody OrderItemRequestDto orderItemRequestDto,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(mileageShopService.orderItem(userDetails.getMember(), mileageItemId, orderItemRequestDto));
    }

    @Operation(summary = "마일리지샵 등록")
    @PostMapping("/mileageshop")
    public ResponseEntity<MsgResponse> createItem(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "requestDto", required = false) CreateMileageItemRequestDto requestDto) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(mileageShopService.createItem(userDetails.getMember(), requestDto, image));
    }

    @Operation(summary = "마일리지샵 전체조회")
    @GetMapping("/mileageshop")
    public ResponseEntity<MsgDataResponse> getItems() {
        return ResponseEntity.status(HttpStatus.OK).body(mileageShopService.getItems());
    }

    @Operation(summary = "마일리지샵 카테고리 조회")
    @GetMapping("/mileageshop/categories/{category}")
    public ResponseEntity<MsgDataResponse> getItemsByCategory(@PathVariable ItemCategoryEnum category) {
        return ResponseEntity.status(HttpStatus.OK).body(mileageShopService.getItemsByCategory(category));
    }

    @Operation(summary = "마일리지샵 이미지 수정")
    @PutMapping("/mileageshop/{id}/image")
    public ResponseEntity<MsgResponse> updateItemImage(@PathVariable Long id, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(mileageShopService.updateItemImage(id, image));
    }

}
