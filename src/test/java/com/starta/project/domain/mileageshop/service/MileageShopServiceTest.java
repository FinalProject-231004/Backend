package com.starta.project.domain.mileageshop.service;

import static org.junit.jupiter.api.Assertions.*;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mileageshop.dto.OrderItemRequestDto;
import com.starta.project.domain.mileageshop.entity.MileageShopItem;
import com.starta.project.domain.mileageshop.repository.MileageShopItemRepository;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.global.messageDto.MsgResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class MileageShopServiceTest {

    @Mock
    private MileageShopItemRepository mileageShopItemRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MileageGetHistoryRepository mileageGetHistoryRepository;

    @InjectMocks
    private MileageShopService mileageShopService;

    @Test
    void whenOrderItem_thenSuccess() {
        // given
        Long memberId = 1L;
        Long itemId = 1L;
        int quantity = 2;
        int price = 500;
        int mileagePoints = 1000;
        String email = "test@example.com";

        Member member = mock(Member.class);
        MemberDetail memberDetail = mock(MemberDetail.class);
        MileageShopItem item = mock(MileageShopItem.class); // mock 객체로 변경
        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(itemId, quantity, email);
        MileageGetHistory mileageGetHistory = mock(MileageGetHistory.class);

        //when

        when(member.getId()).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.getMemberDetail()).thenReturn(memberDetail);
        when(memberDetail.getMileagePoint()).thenReturn(mileagePoints);
        when(mileageShopItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(item.getPrice()).thenReturn(price);
        MsgResponse response = mileageShopService.orderItem(member, orderItemRequestDto);

        // then
        assertNotNull(response);
        assertEquals("구매에 성공 했습니다.", response.getMsg());
        verify(memberDetail).changeMileagePoint(anyInt());
        verify(mileageGetHistoryRepository).save(any(MileageGetHistory.class));
    }
}