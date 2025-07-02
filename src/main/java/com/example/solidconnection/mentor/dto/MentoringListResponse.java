package com.example.solidconnection.mentor.dto;

import java.util.List;

public record MentoringListResponse(
        List<MentoringResponse> mentoringResponseList
) {
    public static MentoringListResponse from(List<MentoringResponse> mentoringResponseList) {
        return new MentoringListResponse(mentoringResponseList);
    }
}
