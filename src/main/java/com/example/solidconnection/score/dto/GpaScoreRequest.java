package com.example.solidconnection.score.dto;

import jakarta.validation.constraints.NotNull;

public record GpaScoreRequest(
        @NotNull(message = "학점을 입력해주세요.")
        Double gpa,

        @NotNull(message = "학점 기준을 입력해주세요.")
        Double gpaCriteria
) {
}
