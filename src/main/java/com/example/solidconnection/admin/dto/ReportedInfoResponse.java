package com.example.solidconnection.admin.dto;

import com.example.solidconnection.report.domain.ReportType;
import com.example.solidconnection.report.domain.TargetType;
import java.time.ZonedDateTime;

public record ReportedInfoResponse(
        ZonedDateTime reportedDate,
        TargetType targetType,
        ReportType reportType
) {

}
