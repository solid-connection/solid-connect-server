package com.example.solidconnection.admin.dto;

import com.example.solidconnection.type.VerifyStatus;

public interface ScoreUpdateRequest {
    VerifyStatus verifyStatus();
    String rejectedReason();
}
