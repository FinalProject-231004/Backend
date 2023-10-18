package com.starta.project.domain.mypage.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.mypage.dto.PurchaseHistoryItemDto;
import com.starta.project.domain.mypage.repository.PurchaseHistoryRepository;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public MsgDataResponse getPurchaseHistory(Member member) {
        return new MsgDataResponse("조회에 성공하셨습니다.", purchaseHistoryRepository.findByMemberDetailIdOrderByOrderedAtDesc(member.getId()).stream().map(PurchaseHistoryItemDto::new));
    }

    public List<Quiz> showUnDisplayQuiz(Member member) {
        return quizRepository.findAllByDisplayIsFalseAndMember(member);
    }
}
