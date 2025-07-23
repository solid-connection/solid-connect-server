package com.example.solidconnection.report.dto;

import com.example.solidconnection.report.domain.ReasonType;
import com.example.solidconnection.report.domain.TargetType;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        long targetId,

        @NotNull(message = "신고 대상을 포함해주세요.")
        TargetType targetType,

        @NotNull(message = "신고 사유를 선택해주세요.")
        ReasonType type
) {

}
