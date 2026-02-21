package com.example.solidconnection.admin.location.country.controller;

import com.example.solidconnection.admin.location.country.dto.AdminCountryCreateRequest;
import com.example.solidconnection.admin.location.country.dto.AdminCountryResponse;
import com.example.solidconnection.admin.location.country.dto.AdminCountryUpdateRequest;
import com.example.solidconnection.admin.location.country.service.AdminCountryService;
import jakarta.validation.Valid;
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

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/admin/countries")
@RestController
public class AdminCountryController {

    private final AdminCountryService adminCountryService;

    @GetMapping
    public ResponseEntity<List<AdminCountryResponse>> getAllCountries() {
        List<AdminCountryResponse> responses = adminCountryService.getAllCountries();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<AdminCountryResponse> createCountry(
            @Valid @RequestBody AdminCountryCreateRequest request
    ) {
        AdminCountryResponse response = adminCountryService.createCountry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{code}")
    public ResponseEntity<AdminCountryResponse> updateCountry(
            @PathVariable String code,
            @Valid @RequestBody AdminCountryUpdateRequest request
    ) {
        AdminCountryResponse response = adminCountryService.updateCountry(code, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCountry(
            @PathVariable String code
    ) {
        adminCountryService.deleteCountry(code);
        return ResponseEntity.noContent().build();
    }
}
