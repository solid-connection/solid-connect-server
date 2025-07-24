package com.example.solidconnection.report.dto;

import com.example.solidconnection.report.domain.ReportType;
import com.example.solidconnection.report.domain.TargetType;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotNull(message = "신고 유형을 선택해주세요.")
        ReportType reportType,

        @NotNull(message = "신고 대상을 포함해주세요.")
        TargetType targetType,

        long targetId
) {

}
