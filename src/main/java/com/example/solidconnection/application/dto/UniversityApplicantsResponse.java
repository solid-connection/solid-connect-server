package com.example.solidconnection.application.dto;

import com.example.solidconnection.entity.UniversityInfoForApply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniversityApplicantsResponse {
    private String koreanName;
    private int studentCapacity;
    private String region;
    private String country;
    private List<ApplicantResponse> applicants;

    public static UniversityApplicantsResponse of(UniversityInfoForApply universityInfoForApply, List<ApplicantResponse> applicant) {
        return new UniversityApplicantsResponse(
                universityInfoForApply.getUniversity().getKoreanName(),
                universityInfoForApply.getStudentCapacity(),
                universityInfoForApply.getUniversity().getRegion().getKoreanName(),
                universityInfoForApply.getUniversity().getCountry().getKoreanName(),
                applicant);
    }
}
