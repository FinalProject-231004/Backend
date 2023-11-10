package com.starta.project.domain.member.repository;

import com.starta.project.domain.member.entity.Report;
import com.starta.project.domain.member.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,Long> {
    boolean existsByReporterIdAndPostedIdAndReportType(Long reporterId, Long entityId, ReportType reportType);

    boolean existsByReporterIdAndReportedIdAndReportType(Long reporterId, Long reportedId, ReportType reportType);

    long countByReportedIdAndReportType(Long id, ReportType reportType);

}
