package com.example.solidconnection.application.dto;

import java.util.List;

public record ApplicationsResponse(
        List<ApplicantsResponse> firstChoice,
        List<ApplicantsResponse> secondChoice,
        List<ApplicantsResponse> thirdChoice) {
}
