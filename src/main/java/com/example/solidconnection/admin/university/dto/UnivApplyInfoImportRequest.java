package com.example.solidconnection.admin.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record UnivApplyInfoImportRequest(
        @NotNull(message = "학기는 필수입니다")
        Long termId,

        @NotNull(message = "대학은 필수입니다")
        Long homeUniversityId,

        @NotBlank(message = "마크다운 텍스트는 필수입니다")
        String markdown,

        @NotNull(message = "컬럼은 필수입니다")
        Map<String, String> columnMappings
) {
}
