package com.example.solidconnection.report.controller;

import com.example.solidconnection.common.resolver.AuthorizedUser;
import com.example.solidconnection.report.dto.ReportRequest;
import com.example.solidconnection.report.service.ReportService;
import com.example.solidconnection.siteuser.domain.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(
            @AuthorizedUser SiteUser siteUser,
            @Valid @RequestBody ReportRequest reportRequest
    ) {
        reportService.createReport(siteUser.getId(), reportRequest);
        return ResponseEntity.ok().build();
    }
}
