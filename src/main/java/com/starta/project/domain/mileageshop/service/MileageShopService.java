package com.starta.project.domain.mileageshop.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mileageshop.dto.CreateMileageItemRequestDto;
import com.starta.project.domain.mileageshop.dto.MileageItemResponseDto;
import com.starta.project.domain.mileageshop.dto.OrderItemRequestDto;
import com.starta.project.domain.mileageshop.entity.ItemCategoryEnum;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import com.starta.project.domain.mileageshop.repository.MileageShopItemRepository;
import com.starta.project.domain.mypage.entity.PurchaseHistory;
import com.starta.project.domain.mypage.repository.PurchaseHistoryRepository;
import com.starta.project.global.aws.AmazonS3Service;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MileageShopService {

    private final MileageShopItemRepository mileageShopItemRepository;
    private final AmazonS3Service amazonS3Service;
    private final MemberRepository memberRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;


    // 마일리지샵 구매
    @Transactional
    public MsgResponse orderItem(Member member, OrderItemRequestDto orderItemRequestDto) {
        // 유저 정보 검색
        Member findMember = findMember(member.getId());

        // 유저 이메일 패턴 인증
        if (orderItemRequestDto.getEmail() == null || !orderItemRequestDto.getEmail().matches("^[a-zA-Z0-9]+@[a-zA-Z0-9\\.]+$")) throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");

        // 마일리지샵 상품 검색
        MileageShopItem findItem = findItem(orderItemRequestDto.getItemId());

        // 유저의 마일리지 포인트
        Integer memberMileagePoint = findMember.getMemberDetail().getMileagePoint();

        // 구매 금액
        Integer totalPrice = findItem.getPrice() * orderItemRequestDto.getQuantity();

        // 유저의 마일리지 포인트가 구매 금액보다 적을 경우
        if (memberMileagePoint < totalPrice) throw new IllegalArgumentException("마일리지가 부족합니다.");

        // 구매 성공 로직
        // 마일리지 차감
        findMember.getMemberDetail().changeMileagePoint(totalPrice);

        // 구매 내역 저장
        PurchaseHistory purchaseHistory = new PurchaseHistory(findItem, findMember.getMemberDetail(), orderItemRequestDto, totalPrice);
        purchaseHistoryRepository.save(purchaseHistory);

        return new MsgResponse("구매에 성공 했습니다.");
    }



    // 마일리지샵 등록
    @Transactional
    public MsgDataResponse createItem(Member member, CreateMileageItemRequestDto requestDto, MultipartFile image) throws IOException {

        // 관리자만 등록 가능
        if (!(member.getRole() == UserRoleEnum.ADMIN)) throw new IllegalArgumentException("잘못된 접근입니다.");

        // 이미지가 없을 경우
        if (image == null) throw new IllegalArgumentException("이미지가 없습니다.");

        // 이미지 업로드
        String imageUrl = amazonS3Service.upload(image);

        MileageShopItem mileageShopItem = new MileageShopItem(requestDto, imageUrl, requestDto.getCategory());
        mileageShopItemRepository.save(mileageShopItem);

        return new MsgDataResponse("마일리지샵 등록에 성공했습니다.", new MileageItemResponseDto(mileageShopItem));
    }

    // 마일리지샵 전체조회
    @Transactional(readOnly = true)
    public MsgDataResponse getItems() {
        return new MsgDataResponse("조회에 성공했습니다.", mileageShopItemRepository.findAll().stream().map(MileageItemResponseDto::new));
    }

    // 마일리지샵 카테고리 조회
    @Transactional(readOnly = true)
    public MsgDataResponse getItemsByCategory(ItemCategoryEnum category) {
        System.out.println("category = " + category);
        return new MsgDataResponse("조회에 성공했습니다.", mileageShopItemRepository.findByCategory(category).stream().map(MileageItemResponseDto::new));
    }

    // 유저 정보 검색
    private Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    // 마일리지샵 상품 검색
    private MileageShopItem findItem(Long id) {
        return mileageShopItemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));
    }


    @Transactional
    public MsgResponse updateItemImage(Long id, MultipartFile image) throws IOException {
        MileageShopItem findItem = findItem(id);
        String oldImageUrl = findItem.getImage();
        if (image.isEmpty()) throw new IllegalArgumentException("이미지가 없습니다.");
        amazonS3Service.deleteFile(oldImageUrl.split("/")[3]);
        String imageUrl = amazonS3Service.upload(image);
        findItem.changeImage(imageUrl);
        return new MsgResponse("이미지 수정에 성공했습니다.");
    }

    public MsgResponse createImages(MultipartFile image) throws IOException {
        // 이미지가 없을 경우
        if (image == null) throw new IllegalArgumentException("이미지가 없습니다.");

        // 이미지 업로드
        String imageUrl = amazonS3Service.upload(image);
        return new MsgResponse("이미지가 업로드되었습니다..");
    }
}
