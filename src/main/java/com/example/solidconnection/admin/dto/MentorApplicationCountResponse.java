package com.example.solidconnection.admin.dto;

public record MentorApplicationCountResponse(
        long approved,
        long pending,
        long rejected
) {

}
