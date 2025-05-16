package com.example.solidconnection.admin.dto;

import com.example.solidconnection.application.domain.VerifyStatus;

public interface ScoreUpdateRequest {
    VerifyStatus verifyStatus();
    String rejectedReason();
}
