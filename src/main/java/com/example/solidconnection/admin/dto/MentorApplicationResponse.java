package com.example.solidconnection.admin.dto;

import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import java.time.ZonedDateTime;

public record MentorApplicationResponse(
        long id,
        String region,
        String country,
        String university,
        String mentorProofUrl,
        MentorApplicationStatus mentorApplicationStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        ZonedDateTime approvedAt
) {

}