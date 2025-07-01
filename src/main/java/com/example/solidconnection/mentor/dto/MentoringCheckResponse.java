package com.example.solidconnection.mentor.dto;

public record MentoringCheckResponse(
        Long mentoringId
) {

    public static MentoringCheckResponse from(Long mentoringId) {
        return new MentoringCheckResponse(mentoringId);
    }
}
