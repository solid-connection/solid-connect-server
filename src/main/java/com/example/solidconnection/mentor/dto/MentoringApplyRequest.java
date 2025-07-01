package com.example.solidconnection.mentor.dto;

import jakarta.validation.constraints.NotNull;

public record MentoringApplyRequest(

        @NotNull(message = "멘토 id를 입력해주세요.")
        Long mentorId
) {
}
