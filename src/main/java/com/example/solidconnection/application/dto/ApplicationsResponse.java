package com.example.solidconnection.application.dto;

import java.util.List;

public record ApplicationsResponse(
        List<UniversityApplicantsResponse> firstChoice,
        List<UniversityApplicantsResponse> secondChoice,
        List<UniversityApplicantsResponse> thirdChoice) {
}
