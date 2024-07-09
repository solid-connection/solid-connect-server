package com.example.solidconnection.university.dto;

import java.util.List;

public record UniversityRecommendsResponse(
        List<UniversityInfoForApplyPreviewResponse> recommendedUniversities) {
}
