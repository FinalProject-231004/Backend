package com.starta.project.domain.member.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long reporterId; // 신고자

    @Column
    private Long reportedId; // 신고당한 사람

    @Column
    private Long postedId; // 신고된 게시글 or 댓글 아이디

    @Column
    @Enumerated(EnumType.STRING)
    private ReportType reportType; // 신고 유형

    public Report(Long reporterId, Long reportedId, Long declaredId, ReportType reportType) {
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.postedId = declaredId;
        this.reportType = reportType;
    }
}
