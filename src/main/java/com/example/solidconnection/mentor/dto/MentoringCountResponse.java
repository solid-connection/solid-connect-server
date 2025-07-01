package com.example.solidconnection.mentor.dto;

public record MentoringCountResponse(
        int mentoringCount
) {

    public static MentoringCountResponse from(int mentoringCount) {
        return new MentoringCountResponse(mentoringCount);
    }
}
