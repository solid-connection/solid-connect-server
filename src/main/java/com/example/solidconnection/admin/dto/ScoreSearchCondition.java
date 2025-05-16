package com.example.solidconnection.admin.dto;

import com.example.solidconnection.application.domain.VerifyStatus;

import java.time.LocalDate;

public record ScoreSearchCondition(
        VerifyStatus verifyStatus,
        String nickname,
        LocalDate createdAt) {
}
