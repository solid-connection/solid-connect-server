package com.example.solidconnection.report.fixture;

import com.example.solidconnection.report.domain.ReasonType;
import com.example.solidconnection.report.domain.Report;
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
    private ReasonType reasonType = ReasonType.ADVERTISEMENT;

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

    public ReportFixtureBuilder reasonType(ReasonType reasonType) {
        this.reasonType = reasonType;
        return this;
    }

    public Report create() {
        Report report = new Report(
                reporterId,
                targetType,
                targetId,
                reasonType
        );
        return reportRepository.save(report);
    }
}
