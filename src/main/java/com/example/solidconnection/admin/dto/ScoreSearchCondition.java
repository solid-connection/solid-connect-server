package com.example.solidconnection.admin.dto;

import com.example.solidconnection.type.VerifyStatus;

import java.time.LocalDate;

public record ScoreSearchCondition(
        VerifyStatus verifyStatus,
        String nickname,
        LocalDate createdAt) {
}
