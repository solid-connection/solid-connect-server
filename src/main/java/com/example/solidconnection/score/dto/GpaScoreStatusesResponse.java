package com.example.solidconnection.score.dto;

import java.util.List;

public record GpaScoreStatusesResponse(
        String homeUniversityName,
        List<GpaScoreStatusResponse> gpaScoreStatusResponseList
) {

    public static GpaScoreStatusesResponse of(
            String homeUniversityName,
            List<GpaScoreStatusResponse> gpaScoreStatusResponseList
    ) {
        return new GpaScoreStatusesResponse(homeUniversityName, gpaScoreStatusResponseList);
    }
}
