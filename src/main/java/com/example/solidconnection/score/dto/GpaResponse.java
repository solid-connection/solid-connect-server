package com.example.solidconnection.score.dto;

import com.example.solidconnection.application.domain.Gpa;

public record GpaResponse(
        double gpa,
        double gpaCriteria,
        String gpaReportUrl
) {

    public static GpaResponse from(Gpa gpa) {
        return new GpaResponse(
                gpa.getGpa(),
                gpa.getGpaCriteria(),
                gpa.getGpaReportUrl()
        );
    }
}
