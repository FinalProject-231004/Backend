package com.starta.project.domain.member.repository;

import com.starta.project.domain.member.entity.Report;
import com.starta.project.domain.member.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report,Long> {
    @Query("SELECT COUNT(r) > 0 " +
            "FROM Report r " +
            "WHERE r.reporterId = :reporterId " +
            "AND r.postedId = :entityId " +
            "AND r.reportType = :reportType")
    boolean existsByReporterIdAndPostedIdAndReportType(@Param("reporterId") Long reporterId,
                                                       @Param("entityId") Long entityId,
                                                       @Param("reportType") ReportType reportType);
    @Query("SELECT COUNT(r) > 0 " +
            "FROM Report r " +
            "WHERE r.reporterId = :reporterId " +
            "AND r.reportedId = :reportedId " +
            "AND r.reportType = :reportType")
    boolean existsByReporterIdAndReportedIdAndReportType(@Param("reporterId") Long reporterId,
                                                         @Param("reportedId") Long reportedId,
                                                         @Param("reportType") ReportType reportType);
    @Query("SELECT COUNT(r) " +
            "FROM Report r " +
            "WHERE r.reportedId = :reportedId " +
            "AND r.reportType = :reportType")
    long countByReportedIdAndReportType(@Param("reportedId") Long reportedId,
                                        @Param("reportType") ReportType reportType);
}
