package com.example.solidconnection.mentor.dto;

import java.util.List;

public record MentoringListResponse(
        List<MentoringResponse> requests
) {
    public static MentoringListResponse from(List<MentoringResponse> requests) {
        return new MentoringListResponse(requests);
    }
}
