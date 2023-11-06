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

    @ManyToOne
    private Member reporter; // 신고자

    @ManyToOne
    private Member reported; // 신고당한 사람

    @Column
    private Long postedId; // 게시글 or 댓글 아이디

    @Column
    private Long commentedId; // 게시글 or 댓글 아이디

    @Column
    @Enumerated(EnumType.STRING)
    private ReportType reportType; // 신고 유형

    public Report(Member reporter, Member reported, Long declaredId, ReportType reportType) {
        this.reporter = reporter;
        this.reported = reported;
        this.reportType = reportType;
        if(reportType == ReportType.POST) {
            this.postedId = declaredId;
        }else if (reportType == ReportType.COMMENT){
            this.commentedId = declaredId;
        }
    }
}
