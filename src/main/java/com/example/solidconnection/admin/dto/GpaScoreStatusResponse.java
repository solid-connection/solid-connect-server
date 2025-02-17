package com.example.solidconnection.admin.dto;

import com.example.solidconnection.type.VerifyStatus;

import java.time.ZonedDateTime;

public record GpaScoreStatusResponse(
        long id,
        GpaResponse gpaResponse,
        VerifyStatus verifyStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
