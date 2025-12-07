package com.example.solidconnection.admin.dto;

import java.time.ZonedDateTime;

import com.example.solidconnection.report.domain.ReportType;

public record ReportedHistoryResponse(
	ZonedDateTime reportedDate,
	ReportType reportType
) {
}
