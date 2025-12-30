package com.example.solidconnection.admin.dto;

import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import java.time.ZonedDateTime;

public record MentorApplicationHistoryResponse(
        long id,
        MentorApplicationStatus mentorApplicationStatus,
        String rejectedReason,
        ZonedDateTime createdAt,
        int applicationOrder
) {

}
