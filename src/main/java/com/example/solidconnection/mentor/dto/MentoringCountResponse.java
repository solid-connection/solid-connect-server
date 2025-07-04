package com.example.solidconnection.mentor.dto;

public record MentoringCountResponse(
        int uncheckedCount
) {

    public static MentoringCountResponse from(int uncheckedCount) {
        return new MentoringCountResponse(uncheckedCount);
    }
}
