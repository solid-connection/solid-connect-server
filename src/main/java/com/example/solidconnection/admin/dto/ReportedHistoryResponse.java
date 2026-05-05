package com.example.solidconnection.admin.dto;

import com.example.solidconnection.report.domain.ReportType;
import java.time.ZonedDateTime;

public record ReportedHistoryResponse(
        ZonedDateTime reportedDate,
        ReportType reportType
) {

}
