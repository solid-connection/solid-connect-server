package com.example.solidconnection.admin.university.dto;

import com.example.solidconnection.university.domain.HostUniversity;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminHostUniversityListResponse(
        List<AdminHostUniversityResponse> hostUniversities,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static AdminHostUniversityListResponse from(Page<HostUniversity> hostUniversityPage) {
        List<AdminHostUniversityResponse> hostUniversities = hostUniversityPage.getContent()
                .stream()
                .map(AdminHostUniversityResponse::from)
                .toList();

        return new AdminHostUniversityListResponse(
                hostUniversities,
                hostUniversityPage.getNumber(),
                hostUniversityPage.getSize(),
                hostUniversityPage.getTotalElements(),
                hostUniversityPage.getTotalPages(),
                hostUniversityPage.hasNext(),
                hostUniversityPage.hasPrevious()
        );
    }
}
