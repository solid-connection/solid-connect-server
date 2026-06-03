package com.example.solidconnection.admin.university.controller;

import com.example.solidconnection.admin.university.dto.AdminHomeUniversityCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityResponse;
import com.example.solidconnection.admin.university.dto.AdminHomeUniversityUpdateRequest;
import com.example.solidconnection.admin.university.service.AdminHomeUniversityService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/admin/home-universities")
@RestController
public class AdminHomeUniversityController {

    private final AdminHomeUniversityService adminHomeUniversityService;

    @GetMapping
    public ResponseEntity<List<AdminHomeUniversityResponse>> getHomeUniversities() {
        return ResponseEntity.ok(adminHomeUniversityService.getAllHomeUniversities());
    }

    @GetMapping("/{home-university-id}")
    public ResponseEntity<AdminHomeUniversityResponse> getHomeUniversity(
            @PathVariable("home-university-id") Long homeUniversityId
    ) {
        return ResponseEntity.ok(adminHomeUniversityService.getHomeUniversity(homeUniversityId));
    }

    @PostMapping
    public ResponseEntity<AdminHomeUniversityResponse> createHomeUniversity(
            @Valid @RequestBody AdminHomeUniversityCreateRequest request
    ) {
        return ResponseEntity.ok(adminHomeUniversityService.createHomeUniversity(request));
    }

    @PutMapping("/{home-university-id}")
    public ResponseEntity<AdminHomeUniversityResponse> updateHomeUniversity(
            @PathVariable("home-university-id") Long homeUniversityId,
            @Valid @RequestBody AdminHomeUniversityUpdateRequest request
    ) {
        return ResponseEntity.ok(adminHomeUniversityService.updateHomeUniversity(homeUniversityId, request));
    }

    @DeleteMapping("/{home-university-id}")
    public ResponseEntity<Void> deleteHomeUniversity(
            @PathVariable("home-university-id") Long homeUniversityId
    ) {
        adminHomeUniversityService.deleteHomeUniversity(homeUniversityId);
        return ResponseEntity.ok().build();
    }
}
