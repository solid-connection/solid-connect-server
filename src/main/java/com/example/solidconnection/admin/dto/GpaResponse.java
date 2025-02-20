package com.example.solidconnection.admin.dto;

public record GpaResponse(
        double gpa,
        double gpaCriteria,
        String gpaReportUrl
) {
}
