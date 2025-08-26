package com.example.solidconnection.mentor.dto;

import com.example.solidconnection.mentor.domain.Mentoring;
import java.util.List;

public record CheckedMentoringsResponse(
        List<Long> checkedMentoringIds
) {

    public static CheckedMentoringsResponse from(List<Mentoring> mentorings) {
        return new CheckedMentoringsResponse(
                mentorings.stream().map(Mentoring::getId).toList()
        );
    }
}
