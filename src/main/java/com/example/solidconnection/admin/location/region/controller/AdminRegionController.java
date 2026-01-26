package com.example.solidconnection.admin.location.region.controller;

import com.example.solidconnection.admin.location.region.dto.AdminRegionCreateRequest;
import com.example.solidconnection.admin.location.region.dto.AdminRegionResponse;
import com.example.solidconnection.admin.location.region.dto.AdminRegionUpdateRequest;
import com.example.solidconnection.admin.location.region.service.AdminRegionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/admin/regions")
@RestController
public class AdminRegionController {

    private final AdminRegionService adminRegionService;

    @GetMapping
    public ResponseEntity<List<AdminRegionResponse>> getAllRegions() {
        List<AdminRegionResponse> responses = adminRegionService.getAllRegions();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<AdminRegionResponse> createRegion(
            @Valid @RequestBody AdminRegionCreateRequest request
    ) {
        AdminRegionResponse response = adminRegionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{code}")
    public ResponseEntity<AdminRegionResponse> updateRegion(
            @PathVariable String code,
            @Valid @RequestBody AdminRegionUpdateRequest request
    ) {
        AdminRegionResponse response = adminRegionService.updateRegion(code, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteRegion(
            @PathVariable String code
    ) {
        adminRegionService.deleteRegion(code);
        return ResponseEntity.noContent().build();
    }
}
