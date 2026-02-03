package com.example.solidconnection.admin.university.controller;

import com.example.solidconnection.admin.university.dto.AdminHostUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityDetailResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityListResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversitySearchCondition;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityUpdateRequest;
import com.example.solidconnection.admin.university.service.AdminHostUniversityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/host-universities")
@RestController
public class AdminHostUniversityController {

    private final AdminHostUniversityService adminHostUniversityService;

    @GetMapping
    public ResponseEntity<AdminHostUniversityListResponse> getHostUniversities(
            AdminHostUniversitySearchCondition condition,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        AdminHostUniversityListResponse response = adminHostUniversityService.getHostUniversities(condition, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{host-university-id}")
    public ResponseEntity<AdminHostUniversityDetailResponse> getHostUniversity(
            @PathVariable("host-university-id") Long hostUniversityId
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.getHostUniversity(hostUniversityId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AdminHostUniversityDetailResponse> createHostUniversity(
            @Valid @RequestBody AdminHostUniversityCreateRequest request
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.createHostUniversity(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{host-university-id}")
    public ResponseEntity<AdminHostUniversityDetailResponse> updateHostUniversity(
            @PathVariable("host-university-id") Long hostUniversityId,
            @Valid @RequestBody AdminHostUniversityUpdateRequest request
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.updateHostUniversity(hostUniversityId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{host-university-id}")
    public ResponseEntity<Void> deleteHostUniversity(
            @PathVariable("host-university-id") Long hostUniversityId
    ) {
        adminHostUniversityService.deleteHostUniversity(hostUniversityId);
        return ResponseEntity.ok().build();
    }
}
