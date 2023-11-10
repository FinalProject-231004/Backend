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

        // 신고자
        validationUtil.findMember(reporterId);
        // 신고한 퀴즈
        Quiz reportedQuiz = validationUtil.findQuiz(quizId);
        // 불량퀴즈 작성유저
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

        // 신고자
        validationUtil.findMember(reporterId);
        // 신고한 댓글
        Comment reportedComment = validationUtil.findComment(commentId);
        // 불량댓글 작성유저
        Member reportedMember = validationUtil.findMember(reportedComment.getMemberId());

        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReport(reporterId, reportedMember.getId(), commentId, ReportType.COMMENT);

        reportedComment.complain(); // 댓글의 신고 횟수 증가
        if(reportedComment.getComplainInt() >= MAX_COMPLAINTS) {
            handleMemberComplaint(reportedMember.getMemberDetail()); // 회원의 신고 횟수 증가 및 처리
            commentRepository.delete(reportedComment);
        }
        return new MsgResponse("댓글 신고 처리되었습니다.");
    }

    @Transactional
    public MsgResponse reportliveChat(String chatNickname, Long reporterId) {
        // 신고자
        Member reportedMember = validationUtil.findMember(reporterId);
        // 불량채팅 작성유저
        MemberDetail reportedMemberDetail = validationUtil.findMemberDetailByNickname(chatNickname);
        Long memberId = reportedMemberDetail.getMember().getId();
        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReportForChat(reporterId, memberId, ReportType.LIVECHAT);

        long reportCount = reportRepository.countByReportedIdAndReportType(memberId, ReportType.LIVECHAT);
        System.out.println("신고횟수 : " + reportCount);

        if (reportCount >= MAX_COMPLAINTS) {
            reportedMemberDetail.setComplaint(); // this.complaint = 3
            reportedMember.setBlock(true); // 회원 차단
            reportedMember.setRole(UserRoleEnum.BLOCK); // 회원 역할 변경
        }
        return new MsgResponse("채팅 신고 처리되었습니다.");
    }

    private void handleMemberComplaint(MemberDetail memberDetail) {
        memberDetail.addComplaint(); // 회원 신고 횟수 증가
        if (memberDetail.getComplaint() >= MAX_COMPLAINTS) {
            Member member = memberDetail.getMember();
            member.setBlock(true); // 회원 차단
            member.setRole(UserRoleEnum.BLOCK); // 회원 역할 변경
        }
    }

     // 중복 신고 확인 및 신고 기록 저장 (퀴즈, 댓글)
    private void validateAndSaveReport(Long reporterId, Long reportedId, Long postId, ReportType reportType) {
        if (reportRepository.existsByReporterIdAndPostedIdAndReportType(reporterId, postId, reportType)) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }
        Report report = new Report(reporterId, reportedId, postId, reportType);
        reportRepository.save(report);
    }

    // 중복 신고 확인 및 신고 기록 저장 (채팅)
    private void validateAndSaveReportForChat(Long reporterId, Long reportedId, ReportType reportType) {
        if (reportRepository.existsByReporterIdAndReportedIdAndReportType(reporterId, reportedId, reportType)) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }
        Report report = new Report(reporterId, reportedId, 0L, reportType);
        reportRepository.save(report);
    }
}
