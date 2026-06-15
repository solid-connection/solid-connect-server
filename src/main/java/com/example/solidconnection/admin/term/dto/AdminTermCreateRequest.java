package com.example.solidconnection.admin.term.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminTermCreateRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d$", message = "학기 이름은 'YYYY-N' 형태여야 합니다. (예: 2026-1)")
        String name
) {
}
