package com.example.solidconnection.admin.dto;

import com.example.solidconnection.mentor.domain.MentorApplicationStatus;
import java.time.LocalDate;

public record MentorApplicationSearchCondition(
        MentorApplicationStatus mentorApplicationStatus,
        String keyword,
        LocalDate createdAt
) {

}