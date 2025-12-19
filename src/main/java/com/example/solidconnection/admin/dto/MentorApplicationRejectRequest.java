package com.example.solidconnection.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record MentorApplicationRejectRequest(
        @NotBlank(message = "거절 사유는 필수입니다")
        String rejectedReason
) {

}
