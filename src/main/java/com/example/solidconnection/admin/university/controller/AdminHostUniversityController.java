package com.example.solidconnection.admin.university.controller;

import com.example.solidconnection.admin.university.dto.AdminHostUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityDetailResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHostUniversitySearchCondition;
import com.example.solidconnection.admin.university.dto.AdminHostUniversityUpdateRequest;
import com.example.solidconnection.admin.university.service.AdminHostUniversityService;
import com.example.solidconnection.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/admin/host-universities")
@RestController
public class AdminHostUniversityController {

    private final AdminHostUniversityService adminHostUniversityService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminHostUniversityResponse>> getHostUniversities(
            AdminHostUniversitySearchCondition condition,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.of(adminHostUniversityService.getHostUniversities(condition, pageable)));
    }

    @GetMapping("/{host-university-id}")
    public ResponseEntity<AdminHostUniversityDetailResponse> getHostUniversity(
            @PathVariable("host-university-id") Long hostUniversityId
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.getHostUniversity(hostUniversityId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminHostUniversityDetailResponse> createHostUniversity(
            @Valid @RequestPart("request") AdminHostUniversityCreateRequest request,
            @RequestPart("logoFile") MultipartFile logoFile,
            @RequestPart("backgroundFile") MultipartFile backgroundFile
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.createHostUniversity(
                request,
                logoFile,
                backgroundFile
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{host-university-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminHostUniversityDetailResponse> updateHostUniversity(
            @PathVariable("host-university-id") Long hostUniversityId,
            @Valid @RequestPart("request") AdminHostUniversityUpdateRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestPart(value = "backgroundFile", required = false) MultipartFile backgroundFile
    ) {
        AdminHostUniversityDetailResponse response = adminHostUniversityService.updateHostUniversity(
                hostUniversityId,
                request,
                logoFile,
                backgroundFile
        );
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
