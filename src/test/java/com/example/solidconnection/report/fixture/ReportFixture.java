package com.example.solidconnection.report.fixture;

import com.example.solidconnection.report.domain.Report;
import com.example.solidconnection.report.domain.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class ReportFixture {

    private final ReportFixtureBuilder reportFixtureBuilder;

    public Report 신고(long reporterId, long reportedId, TargetType targetType, long targetId) {
        return reportFixtureBuilder.report()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .targetType(targetType)
                .targetId(targetId)
                .create();
    }
}
