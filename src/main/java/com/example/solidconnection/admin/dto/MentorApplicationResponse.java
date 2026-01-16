package com.example.solidconnection.admin.dto;

import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import com.example.solidconnection.mentor.domain.UniversitySelectType;
import java.time.ZonedDateTime;

public record MentorApplicationResponse(
        long id,
        String region,
        String country,
        String university,
        UniversitySelectType universitySelectType,
        String mentorProofUrl,
        MentorApplicationStatus mentorApplicationStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        ZonedDateTime approvedAt
) {

}