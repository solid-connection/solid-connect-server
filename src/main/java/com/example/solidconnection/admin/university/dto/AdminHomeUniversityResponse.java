package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HomeUniversity;

public record AdminHomeUniversityResponse(
        long id,
        String name,
        String emailDomain
        String name,
        int maxChoiceCount
) {

    public static AdminHomeUniversityResponse from(HomeUniversity homeUniversity) {
        return new AdminHomeUniversityResponse(
                homeUniversity.getId(),
                homeUniversity.getName(),
                homeUniversity.getEmailDomain()
                homeUniversity.getName(),
                homeUniversity.getMaxChoiceCount()
        );
    }
}
