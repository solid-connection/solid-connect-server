package com.example.solidconnection.mentor.dto;

public record MentoringApprovedEvent(
        long mentoringId,
        long mentorId,
        long menteeId
) {

    public static MentoringApprovedEvent of(long mentoringId, long mentorId, long menteeId) {
        return new MentoringApprovedEvent(mentoringId, mentorId, menteeId);
    }
}
