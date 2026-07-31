package com.example.solidconnection.application.dto;

import java.util.List;

public record ApplicationPreviewResponse(
        long totalUniversityCount,
        List<ApplicationUniversityPreviewResponse> universities) {
}
