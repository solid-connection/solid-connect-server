package com.example.solidconnection.application.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationsResponse {
    private List<UniversityApplicantsResponse> firstChoice;
    private List<UniversityApplicantsResponse> secondChoice;
}
