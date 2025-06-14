package com.example.solidconnection.admin.dto;

import com.example.solidconnection.application.domain.VerifyStatus;

import java.time.ZonedDateTime;

public record LanguageTestScoreStatusResponse(
        long id,
        LanguageTestResponse languageTestResponse,
        VerifyStatus verifyStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
