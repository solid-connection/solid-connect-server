package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HomeUniversity;

public record AdminHomeUniversityResponse(
        Long id,
        String name
) {

    public static AdminHomeUniversityResponse from(HomeUniversity homeUniversity) {
        return new AdminHomeUniversityResponse(
                homeUniversity.getId(),
                homeUniversity.getName()
        );
    }
}
