package com.example.solidconnection.admin.dto;

import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import java.time.ZonedDateTime;

public record MentorApplicationHistoryInfoResponse(
        MentorApplicationStatus mentorApplicationStatus,
        String rejectedReason,
        ZonedDateTime createdAt
) {

}
