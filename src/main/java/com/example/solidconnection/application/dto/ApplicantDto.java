package com.example.solidconnection.application.dto;

import com.example.solidconnection.application.domain.Application;
import com.example.solidconnection.type.LanguageTestType;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicantDto {
    private String nicknameForApply;
    private float gpa;
    private LanguageTestType testType;
    private String testScore;
    private boolean isMine;

    public static ApplicantDto fromEntity(Application application, boolean isMine) {
        return ApplicantDto.builder()
                .nicknameForApply(application.getNicknameForApply())
                .gpa(application.getGpa().getGpa())
                .testType(application.getLanguageTest().getLanguageTestType())
                .testScore(application.getLanguageTest().getLanguageTestScore())
                .isMine(isMine)
                .build();
    }
}
