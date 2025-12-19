package com.example.solidconnection.admin.dto;

public record MentorApplicationCountResponse(
        long approvedCount,
        long pendingCount,
        long rejectedCount
) {

}
