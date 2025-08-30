package com.example.solidconnection.mentor.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.example.solidconnection.mentor.domain.Mentoring;
import com.fasterxml.jackson.annotation.JsonInclude;

public record MentoringConfirmResponse(
        long mentoringId,

        @JsonInclude(NON_NULL)
        Long chatRoomId
) {

    public static MentoringConfirmResponse from(Mentoring mentoring, Long chatRoomId) {
        return new MentoringConfirmResponse(mentoring.getId(), chatRoomId);
    }
}
