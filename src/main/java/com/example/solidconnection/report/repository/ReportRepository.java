package com.example.solidconnection.report.repository;

import java.util.Optional;

import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(long reporterId, TargetType targetType, long targetId);

    Optional<Report> findByTargetId(long targetId);
}
