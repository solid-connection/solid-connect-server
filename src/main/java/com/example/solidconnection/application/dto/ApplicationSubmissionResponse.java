package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import java.util.List;

public record ApplicationSubmissionResponse(
        int totalApplyCount,
        int applyCount,
        UnivApplyInfoResponse appliedUniversities
) {

    public static ApplicationSubmissionResponse of(int totalApplyCount, Application application, List<UnivApplyInfo> uniApplyInfos) {
        return new ApplicationSubmissionResponse(
                totalApplyCount,
                application.getUpdateCount(),
                UnivApplyInfoResponse.of(application, uniApplyInfos)
        );
    }
}
