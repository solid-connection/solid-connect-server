package com.example.solidconnection.admin.dto;

import com.example.solidconnection.application.domain.Gpa;
import com.example.solidconnection.type.VerifyStatus;

import java.time.ZonedDateTime;

public record GpaScoreStatusResponse(
        long id,
        Gpa gpa,
        VerifyStatus verifyStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
