package com.example.solidconnection.admin.university.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminHomeUniversityUpdateRequest(
        @NotBlank(message = "협정 대학명은 필수입니다")
        @Size(max = 100, message = "협정 대학명은 100자 이하여야 합니다")
        String name,

        @Min(value = 1, message = "최대 지망 수는 1 이상이어야 합니다")
        int maxChoiceCount,

        @Size(max = 100, message = "이메일 도메인은 100자 이하여야 합니다")
        @Pattern(
                regexp = "^[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?)+$",
                message = "올바른 이메일 도메인 형식이 아닙니다 (예: inha.edu, inu.ac.kr)"
        )
        String emailDomain
) {

}
