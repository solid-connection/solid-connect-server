package com.example.solidconnection.home.dto;

import java.util.List;

public record PersonalHomeInfoResponse(
        List<RecommendedUniversityResponse> recommendedUniversities) {
}
