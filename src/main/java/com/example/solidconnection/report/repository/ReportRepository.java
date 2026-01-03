package com.example.solidconnection.report.repository;

import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(long reporterId, TargetType targetType, long targetId);

    boolean existsByReportedId(long reportedId);
}
