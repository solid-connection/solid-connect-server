package com.example.solidconnection.admin.dto;

import java.time.ZonedDateTime;

import com.example.solidconnection.report.domain.ReportType;
import com.example.solidconnection.report.domain.TargetType;

public record ReportedInfoResponse(
	ZonedDateTime reportedDate,
	TargetType targetType,
	ReportType reportType
) {
}
