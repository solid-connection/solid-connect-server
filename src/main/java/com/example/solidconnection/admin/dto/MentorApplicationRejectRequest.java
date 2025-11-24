package com.example.solidconnection.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MentorApplicationRejectRequest(
        @NotBlank(message = "거절 사유는 필수입니다")
        @Size(max = 200, message = "거절 사유는 200자를 초과할 수 없습니다")
        String rejectedReason
) {

}
