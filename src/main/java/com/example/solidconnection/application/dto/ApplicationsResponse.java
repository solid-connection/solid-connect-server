package com.example.solidconnection.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "1지망과 2지망 대학과 그 대학에 지원한 지원자 정보")
public record ApplicationsResponse(

        @ArraySchema(arraySchema = @Schema(description = "1지망 대학에 지원한 지원자 목록"))
        List<UniversityApplicantsResponse> firstChoice,

        @ArraySchema(arraySchema = @Schema(description = "2지망 대학에 지원한 지원자 목록"))
        List<UniversityApplicantsResponse> secondChoice) {
}
