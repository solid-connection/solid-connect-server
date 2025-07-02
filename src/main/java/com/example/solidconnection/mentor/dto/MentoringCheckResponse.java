package com.example.solidconnection.mentor.dto;

public record MentoringCheckResponse(
        long mentoringId
) {

    public static MentoringCheckResponse from(long mentoringId) {
        return new MentoringCheckResponse(mentoringId);
    }
}
