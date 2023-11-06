package com.starta.project.domain.mypage.service;

import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.mypage.dto.MileageGetHistoryDto;
import com.starta.project.domain.mypage.dto.MyPageMemberInfoDto;
import com.starta.project.domain.mypage.dto.PurchaseHistoryItemDto;
import com.starta.project.domain.mypage.entity.AttendanceCheck;
import com.starta.project.domain.mypage.entity.MileageGetHistory;
import com.starta.project.domain.mypage.entity.TypeEnum;
import com.starta.project.domain.mypage.repository.AttendanceCheckRepository;
import com.starta.project.domain.mypage.repository.MileageGetHistoryRepository;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgDataResponse;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final QuizRepository quizRepository;
    private final AttendanceCheckRepository attendanceCheckRepository;
    private final MemberRepository memberRepository;
    private final MileageGetHistoryRepository mileageGetHistoryRepository;

    @Transactional(readOnly = true)
    public MsgDataResponse getPurchaseHistory(Member member) {
        return new MsgDataResponse("조회에 성공하셨습니다.",
                mileageGetHistoryRepository.findByMemberDetailIdOrderByDateDesc(member.getId()).stream()
                        .filter(historyItem -> historyItem.getPoints() < 0) // points 컬럼이 음수인 항목만 필터링
                        .map(PurchaseHistoryItemDto::new)
                        .collect(Collectors.toList()));
    }

    //미 게시 퀴즈 찾아옴
    public List<Quiz> showUnDisplayQuiz(Member member) {
        return quizRepository.findAllByDisplayIsFalseAndMemberId(member.getId());
    }

    // 출석 체크
    @Transactional
    public MsgResponse attendanceCheck(Member member) {
        Member findMember = findMember(member.getId());

        // 현재 날짜 확인
        LocalDate today = LocalDate.now();

        // 중복 체크
        Optional<AttendanceCheck> existingCheck = attendanceCheckRepository.findByMemberAndCheckDate(member, today);
        if (existingCheck.isPresent()) {
            throw new IllegalArgumentException("중복 출석 체크 입니다.");
        }

        // 출석 체크 저장
        AttendanceCheck attendanceCheck = new AttendanceCheck(member);
        attendanceCheckRepository.save(attendanceCheck);

        // 마일리지 지급
        findMember.getMemberDetail().gainMileagePoint(100);

        // 마일리지 내역 업데이트
        MileageGetHistory mileageGetHistory = new MileageGetHistory("출석체크", TypeEnum.ATTENDANCE_CHECK, 100, findMember.getMemberDetail());
        mileageGetHistoryRepository.save(mileageGetHistory);


        // 출석 체크 결과 반환
        return new MsgResponse("출석체크에 성공하셨습니다.");
    }

    // 회원 정보 조회 (헤더 정보값)
    public MsgDataResponse memberInfo(Member member) {
        Member findMember = findMember(member.getId());
        MyPageMemberInfoDto myPageMemberInfoDto = new MyPageMemberInfoDto(findMember);
        return new MsgDataResponse("조회에 성공하셨습니다.", myPageMemberInfoDto);
    }

    // 마일리지 적립 내역
    @Transactional(readOnly = true)
    public MsgDataResponse mileageGetHistory(Member member) {
        Member findMember = findMember(member.getId());
        return new MsgDataResponse("조회에 성공하셨습니다.", mileageGetHistoryRepository.findByMemberDetailIdOrderByDateDesc(findMember.getMemberDetail().getId())
                .stream()
                .filter(mileage -> mileage.getPoints() > 0) // points 컬럼이 양수인 요소만 필터링
                .map(MileageGetHistoryDto::new)
                .collect(Collectors.toList()));
    }

    public MsgDataResponse spendHistory(Member member) {
        MemberDetail memberDetail = member.getMemberDetail();
        return new MsgDataResponse("조회에 성공하셨습니다.", mileageGetHistoryRepository.findAllByMemberDetailAndTypeOrderByDateDesc(memberDetail, TypeEnum.SPEND));
    }

    // 유저 정보 검색
    private Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
