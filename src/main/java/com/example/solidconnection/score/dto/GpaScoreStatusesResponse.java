package com.example.solidconnection.score.dto;

import java.util.List;

public record GpaScoreStatusesResponse(
        List<GpaScoreStatusResponse> gpaScoreStatusResponseList
) {

    public static GpaScoreStatusesResponse from(List<GpaScoreStatusResponse> gpaScoreStatusResponseList) {
        return new GpaScoreStatusesResponse(gpaScoreStatusResponseList);
    }
}
