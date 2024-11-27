package com.example.solidconnection.score.dto;

import java.util.List;

public record GpaScoreStatusResponse(
        List<GpaScoreStatus> gpaScoreStatusList
) {
}
