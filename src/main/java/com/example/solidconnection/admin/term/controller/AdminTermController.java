package com.example.solidconnection.admin.term.controller;

import com.example.solidconnection.admin.term.dto.AdminTermCreateRequest;
import com.example.solidconnection.admin.term.dto.AdminTermResponse;
import com.example.solidconnection.admin.term.service.AdminTermService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/terms")
@RestController
public class AdminTermController {

    private final AdminTermService adminTermService;

    @GetMapping
    public ResponseEntity<List<AdminTermResponse>> getAllTerms() {
        List<AdminTermResponse> responses = adminTermService.getAllTerms();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<AdminTermResponse> createTerm(
            @Valid @RequestBody AdminTermCreateRequest request
    ) {
        AdminTermResponse response = adminTermService.createTerm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AdminTermResponse> activateTerm(
            @PathVariable Long id
    ) {
        AdminTermResponse response = adminTermService.activateTerm(id);
        return ResponseEntity.ok(response);
    }
}
