package com.example.solidconnection.admin.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminHomeUniversityUpdateRequest(
        @NotBlank(message = "협정 대학명은 필수입니다")
        @Size(max = 100, message = "협정 대학명은 100자 이하여야 합니다")
        String name,
        @Size(max = 100, message = "이메일 도메인은 100자 이하여야 합니다")
        String emailDomain
        String name,

        @Min(value = 1, message = "최대 지망 수는 1 이상이어야 합니다")
        int maxChoiceCount
) {

}
