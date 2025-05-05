package com.example.solidconnection.support.fixture;

import com.example.solidconnection.university.domain.UniversityInfoForApply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniversityInfoForApplyFixture {

    private final UniversityInfoForApplyFixtureBuilder universityInfoForApplyFixtureBuilder;
    private final UniversityFixture universityFixture;

    public UniversityInfoForApply 괌대학_A_지원_정보() {
        return universityInfoForApplyFixtureBuilder.universityInfoForApply()
                .term("2024-1")
                .koreanName("괌대학 A 지원 정보")
                .university(universityFixture.괌_대학())
                .create();
    }
}
