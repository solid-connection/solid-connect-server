package com.example.solidconnection.report.service;

import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.common.exception.ErrorCode;
import com.example.solidconnection.community.post.repository.PostRepository;
import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.dto.ReportRequest;
import com.example.solidconnection.report.repository.ReportRepository;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final SiteUserRepository siteUserRepository;
    private final PostRepository postRepository;

    @Transactional
    public void createReport(long reporterId, ReportRequest request) {
        validateReporterExists(reporterId);
        validateTargetExists(request.targetType(), request.targetId());
        validateFirstReportByUser(reporterId, request.targetType(), request.targetId());

        Report report = new Report(reporterId, request.reportType(), request.targetType(), request.targetId());
        reportRepository.save(report);
    }

    private void validateReporterExists(long reporterId) {
        if (!siteUserRepository.existsById(reporterId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateTargetExists(TargetType targetType, long targetId) {
        if (targetType == TargetType.POST && !postRepository.existsById(targetId)) {
            throw new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND);
        }
    }

    private void validateFirstReportByUser(long reporterId, TargetType targetType, long targetId) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED_BY_CURRENT_USER);
        }
    }
}
