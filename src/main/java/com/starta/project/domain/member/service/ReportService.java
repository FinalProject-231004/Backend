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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MsgResponse> reportPost(Long quizId, Long reporterId) {
        // 신고자
        validationUtil.findMember(reporterId);
        // 신고한 퀴즈
        Quiz reportedQuiz = validationUtil.findQuiz(quizId);

        // 신고자가 퀴즈 작성자와 동일한지 확인
        ResponseEntity<MsgResponse> body = getMsgResponseResponseEntity(reporterId, reportedQuiz.getMemberId());
        if (body != null) return body;

        // 불량퀴즈 작성유저
        Member reportedMember = validationUtil.findMember(reportedQuiz.getMemberId());

        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReport(reporterId, reportedMember.getId(), quizId, ReportType.QUIZ);

        reportedQuiz.complain(); // 퀴즈의 신고 횟수 증가
        if(reportedQuiz.getComplainInt() >= MAX_COMPLAINTS) {
            handleMemberComplaint(reportedMember.getMemberDetail()); // 회원의 신고 횟수 증가 및 처리
        }

        return ResponseEntity.ok(new MsgResponse("게시글 신고 처리되었습니다."));
    }

    // 댓글 신고
    @Transactional
    public ResponseEntity<MsgResponse> reportComment(Long commentId, Long reporterId) {
        // 신고자
        validationUtil.findMember(reporterId);
        // 신고한 댓글
        Comment reportedComment = validationUtil.findComment(commentId);

        // 신고자가 댓글 작성자와 동일한지 확인
        ResponseEntity<MsgResponse> body = getMsgResponseResponseEntity(reporterId, reportedComment.getMemberId());
        if (body != null) return body;

        // 불량댓글 작성유저
        Member reportedMember = validationUtil.findMember(reportedComment.getMemberId());

        // 중복 신고 확인 및 신고 기록 저장
        validateAndSaveReport(reporterId, reportedMember.getId(), commentId, ReportType.COMMENT);

        reportedComment.complain(); // 댓글의 신고 횟수 증가
        if(reportedComment.getComplainInt() >= MAX_COMPLAINTS) {
            handleMemberComplaint(reportedMember.getMemberDetail()); // 회원의 신고 횟수 증가 및 처리
            commentRepository.delete(reportedComment);
        }
        return ResponseEntity.ok(new MsgResponse("댓글 신고 처리되었습니다."));
    }

    @Transactional
    public ResponseEntity<MsgResponse> reportliveChat(String chatNickname, Long reporterId) {
        // 신고자
        Member reportedMember = validationUtil.findMember(reporterId);
        // 불량채팅 작성유저
        MemberDetail reportedMemberDetail = validationUtil.findMemberDetailByNickname(chatNickname);
        Long reportedMemberId = reportedMemberDetail.getMember().getId();

        // 신고자가 채팅 참여자와 동일한지 확인
        ResponseEntity<MsgResponse> body = getMsgResponseResponseEntity(reporterId, reportedMemberId);
        if (body != null) return body;

        // 중복 신고 확인 및 신고 기록 저장
        System.out.println("시작");
        validateAndSaveReportForChat(reporterId, reportedMemberId);
        System.out.println("끝");

        long reportCount = reportRepository.countByReportedIdAndReportType(reportedMemberId, ReportType.LIVECHAT);
        System.out.println("신고횟수 : " + reportCount);

        if (reportCount >= MAX_COMPLAINTS) {
            reportedMemberDetail.setComplaint(); // this.complaint = 3
            reportedMember.setBlock(true); // 회원 차단
            reportedMember.setRole(UserRoleEnum.BLOCK); // 회원 역할 변경
        }
        return ResponseEntity.ok(new MsgResponse("채팅 신고 처리되었습니다."));
    }

    // 본인은 신고불가
    private static ResponseEntity<MsgResponse> getMsgResponseResponseEntity(Long reporterId, Long reportedMemberId) {
        if (reportedMemberId.equals(reporterId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MsgResponse("본인은 신고할 수 없습니다."));
        }
        return null;
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
    private void validateAndSaveReportForChat(Long reporterId, Long reportedId) {
        if (reportRepository.existsByReporterIdAndReportedIdAndReportType(reporterId, reportedId, ReportType.LIVECHAT)) {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        }
        Report report = new Report(reporterId, reportedId, -1L, ReportType.LIVECHAT);
        reportRepository.save(report);
    }
}
