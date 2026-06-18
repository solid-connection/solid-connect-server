package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HomeUniversity;

public record AdminHomeUniversityResponse(
        long id,
        String name,
        int maxChoiceCount,
        String emailDomain
) {

    public static AdminHomeUniversityResponse from(HomeUniversity homeUniversity) {
        return new AdminHomeUniversityResponse(
                homeUniversity.getId(),
                homeUniversity.getName(),
                homeUniversity.getMaxChoiceCount(),
                homeUniversity.getEmailDomain()
        );
    }
}
