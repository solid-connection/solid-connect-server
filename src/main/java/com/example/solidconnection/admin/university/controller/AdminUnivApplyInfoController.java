package com.example.solidconnection.admin.university.controller;

import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoCreateRequest;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoResponse;
import com.example.solidconnection.admin.university.dto.AdminUnivApplyInfoUpdateRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoFieldResponse;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportRequest;
import com.example.solidconnection.admin.university.dto.UnivApplyInfoImportResponse;
import com.example.solidconnection.admin.university.service.AdminUnivApplyInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/univ-apply-infos")
@RestController
public class AdminUnivApplyInfoController {

    private final AdminUnivApplyInfoService adminUnivApplyInfoService;

    @GetMapping("/fields")
    public ResponseEntity<UnivApplyInfoFieldResponse> getFields() {
        return ResponseEntity.ok(adminUnivApplyInfoService.getFields());
    }

    @PostMapping("/import")
    public ResponseEntity<UnivApplyInfoImportResponse> importUnivApplyInfos(
            @Valid @RequestBody UnivApplyInfoImportRequest request
    ) {
        return ResponseEntity.ok(adminUnivApplyInfoService.importUnivApplyInfos(request));
    }

    @PostMapping
    public ResponseEntity<AdminUnivApplyInfoResponse> createUnivApplyInfo(
            @Valid @RequestBody AdminUnivApplyInfoCreateRequest request
    ) {
        return ResponseEntity.ok(adminUnivApplyInfoService.createUnivApplyInfo(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdminUnivApplyInfoResponse> updateUnivApplyInfo(
            @PathVariable Long id,
            @Valid @RequestBody AdminUnivApplyInfoUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUnivApplyInfoService.updateUnivApplyInfo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnivApplyInfo(
            @PathVariable Long id
    ) {
        adminUnivApplyInfoService.deleteUnivApplyInfo(id);
        return ResponseEntity.ok().build();
    }
}
