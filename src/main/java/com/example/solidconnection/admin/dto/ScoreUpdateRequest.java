package com.example.solidconnection.admin.dto;

import com.example.solidconnection.common.VerifyStatus;

public interface ScoreUpdateRequest {
    VerifyStatus verifyStatus();
    String rejectedReason();
}
