package com.example.solidconnection.admin.dto;

import com.example.solidconnection.score.domain.GpaScore;
import jakarta.validation.constraints.NotNull;

public record GpaUpdateRequest(
        @NotNull(message = "GPA를 입력해주세요.")
        Double gpa,

        @NotNull(message = "GPA 기준을 입력해주세요.")
        Double gpaCriteria
) {
    public static GpaUpdateResponse of(GpaScore gpaScore) {
        return new GpaUpdateResponse(
                gpaScore.getId(),
                gpaScore.getGpa().getGpa(),
                gpaScore.getGpa().getGpaCriteria(),
                gpaScore.getVerifyStatus()
        );
    }
}
