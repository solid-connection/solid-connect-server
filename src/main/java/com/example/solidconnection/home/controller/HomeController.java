package com.example.solidconnection.home.controller;

import com.example.solidconnection.home.dto.PersonalHomeInfoResponse;
import com.example.solidconnection.home.dto.RecommendedUniversityResponse;
import com.example.solidconnection.university.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final UniversityService universityService;

    @GetMapping
    public ResponseEntity<PersonalHomeInfoResponse> getHomeInfo(Principal principal) {
        List<RecommendedUniversityResponse> recommendedUniversities;
        if (principal == null) {
            recommendedUniversities = universityService.getGeneralRecommends();
        } else {
            recommendedUniversities = universityService.getPersonalRecommends(principal.getName());
        }
        return ResponseEntity.ok(new PersonalHomeInfoResponse(recommendedUniversities));
    }
}
