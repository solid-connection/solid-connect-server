package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;

public record ApplicationSubmissionResponse(
        int applyCount
) {
    // 지원횟수는 1회부터 시작하므로 1을 더해줌
    public static ApplicationSubmissionResponse from(Application application) {
        return new ApplicationSubmissionResponse(application.getUpdateCount() + 1);
    }
}
