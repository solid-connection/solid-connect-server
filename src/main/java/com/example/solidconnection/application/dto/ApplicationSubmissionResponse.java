package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;

public record ApplicationSubmissionResponse(
        int applyCount
) {
    public static ApplicationSubmissionResponse from(Application application) {
        return new ApplicationSubmissionResponse(application.getUpdateCount());
    }
}
