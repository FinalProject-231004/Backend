package com.starta.project.domain.member.service;

import com.starta.project.domain.member.entity.*;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.domain.member.repository.ReportRepository;
import com.starta.project.domain.member.util.ValidationUtil;
import com.starta.project.domain.quiz.entity.Comment;
import com.starta.project.domain.quiz.entity.Quiz;
import com.starta.project.domain.quiz.repository.CommentRepository;
import com.starta.project.domain.quiz.repository.QuizRepository;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final ValidationUtil validationUtil;

    // 신고 횟수 MAX
    private final Integer MAX_COMPLAINTS = 3;

    // 게시글 신고
    @Transactional
    public MsgResponse reportPost(Long quizId, Long reporterId) {

        // 신고자와 신고된 게시글, 게시 유저의 유효성 검증
        validationUtil.findMember(reporterId);
        Quiz reportedQuiz = validationUtil.findQuiz(quizId);
        Member reportedMember = validationUtil.findMember(reportedQuiz.getMemberId());

        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReport(reporterId, reportedMember.getId(), quizId, ReportType.QUIZ);

        reportedQuiz.complain(); // 퀴즈의 신고 횟수 증가
        if(reportedQuiz.getComplainInt() >= MAX_COMPLAINTS) {
            handleMemberComplaint(reportedMember.getMemberDetail()); // 회원의 신고 횟수 증가 및 처리
        }

        return new MsgResponse("게시글 신고 처리되었습니다.");
    }

    // 댓글 신고
    @Transactional
    public MsgResponse reportComment(Long commentId, Long reporterId) {

        // 신고자와 신고된 댓글, 댓글 유저를 검증
        Comment reportedComment = validationUtil.findComment(commentId);  // 신고한 댓글
        Member reporter = validationUtil.findMember(reporterId);   // 신고자
        Member reportedMember = validationUtil.findMember(reportedComment.getMemberId()); // 불량댓글 작성유저

        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReport(reporterId, reportedMember.getId(), commentId, ReportType.COMMENT);

        reportedComment.complain(); // 댓글의 신고 횟수 증가
        if(reportedComment.getComplainInt() >= MAX_COMPLAINTS) {
            handleMemberComplaint(reportedMember.getMemberDetail()); // 회원의 신고 횟수 증가 및 처리
            commentRepository.delete(reportedComment);
        }
        return new MsgResponse("댓글 신고 처리되었습니다.");
    }

    private void handleMemberComplaint(MemberDetail memberDetail) {
        memberDetail.addComplaint(); // 회원 신고 횟수 증가

        if (memberDetail.getComplaint() >= MAX_COMPLAINTS) {
            Member member = memberDetail.getMember();
            member.setBlock(true); // 회원 차단
            member.setRole(UserRoleEnum.BLOCK); // 회원 역할 변경
        }
    }

     // 중복 신고 확인 및 신고 기록 저장
    private void validateAndSaveReport(Long reporterId, Long reportedId, Long postId, ReportType reportType) {
        if (reportRepository.existsByReporterIdAndPostedIdAndReportType(reporterId, postId, reportType)) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }
        Report report = new Report(reporterId, reportedId, postId, reportType);
        reportRepository.save(report);
    }
}
