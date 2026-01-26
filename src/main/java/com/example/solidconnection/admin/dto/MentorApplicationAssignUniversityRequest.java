package com.example.solidconnection.admin.dto;

import jakarta.validation.constraints.NotNull;

public record MentorApplicationAssignUniversityRequest(
        @NotNull(message = "대학 ID 는 필수입니다.")
        Long universityId
) {

}
