package com.example.solidconnection.report.fixture;

import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.ReportType;
import com.example.solidconnection.report.domain.TargetType;
import com.example.solidconnection.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ReportFixtureBuilder {

    private final ReportRepository reportRepository;

    private long reporterId;
    private TargetType targetType;
    private long targetId;
    private ReportType reportType = ReportType.ADVERTISEMENT;

    public ReportFixtureBuilder report() {
        return new ReportFixtureBuilder(reportRepository);
    }

    public ReportFixtureBuilder reporterId(long reporterId) {
        this.reporterId = reporterId;
        return this;
    }

    public ReportFixtureBuilder targetType(TargetType targetType) {
        this.targetType = targetType;
        return this;
    }

    public ReportFixtureBuilder targetId(long targetId) {
        this.targetId = targetId;
        return this;
    }

    public ReportFixtureBuilder reasonType(ReportType reportType) {
        this.reportType = reportType;
        return this;
    }

    public Report create() {
        Report report = new Report(
                reporterId,
                reportType,
                targetType,
                targetId
        );
        return reportRepository.save(report);
    }
}
