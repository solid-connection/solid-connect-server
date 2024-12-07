package com.example.solidconnection.university.dto;

import com.example.solidconnection.university.domain.UniversityInfoForApply;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

@Schema(description = "대학 미리보기 응답")
public record UniversityInfoForApplyPreviewResponse(
        @Schema(description = "대학 지원을 위한 정보 id", example = "1")
        long id,

        @Schema(description = "모집 시기", example = "2024-2")
        String term,

        @Schema(description = "국문 이름", example = "그라츠대학")
        String koreanName,

        @Schema(description = "지역", example = "유럽")
        String region,

        @Schema(description = "국가", example = "오스트리아")
        String country,

        @Schema(description = "대학 로고 이미지 URL", example = "http://example.com/logo.jpg")
        String logoImageUrl,

        @Schema(description = "모집 인원", example = "2")
        int studentCapacity,

        @ArraySchema(arraySchema = @Schema(description = "어학시험 요구사항"))
        List<LanguageRequirementResponse> languageRequirements) {

    public static UniversityInfoForApplyPreviewResponse from(UniversityInfoForApply universityInfoForApply) {
        List<LanguageRequirementResponse> languageRequirementResponses = new java.util.ArrayList<>(
                universityInfoForApply.getLanguageRequirements().stream()
                        .map(LanguageRequirementResponse::from)
                        .toList());
        Collections.sort(languageRequirementResponses);

        return new UniversityInfoForApplyPreviewResponse(
                universityInfoForApply.getId(),
                universityInfoForApply.getTerm(),
                universityInfoForApply.getKoreanName(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                universityInfoForApply.getUniversity().getLogoImageUrl(),
                universityInfoForApply.getStudentCapacity(),
                languageRequirementResponses
        );
    }
}
