package com.example.solidconnection.university.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "추천 대학 목록 응답")
public record UniversityRecommendsResponse(
        @ArraySchema(arraySchema = @Schema(description = "추천된 대학 목록", implementation = UniversityInfoForApplyPreviewResponse.class))
        List<UniversityInfoForApplyPreviewResponse> recommendedUniversities) {
}
