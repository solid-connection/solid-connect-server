package com.example.solidconnection.university.fixture;

import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.LanguageTestType;
import com.example.solidconnection.university.domain.UnivApplyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LanguageRequirementFixture {

    private final LanguageRequirementFixtureBuilder languageRequirementFixtureBuilder;

    public LanguageRequirement 토플_80(UnivApplyInfo universityInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("80")
                .universityInfoForApply(universityInfo)
                .create();
    }

    public LanguageRequirement 토플_70(UnivApplyInfo universityInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("70")
                .universityInfoForApply(universityInfo)
                .create();
    }

    public LanguageRequirement 토익_800(UnivApplyInfo universityInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
    }

    public LanguageRequirement 토익_900(UnivApplyInfo universityInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("900")
                .universityInfoForApply(universityInfo)
                .create();
    }

    public LanguageRequirement JLPT_N2(UnivApplyInfo universityInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.JLPT)
                .minScore("N2")
                .universityInfoForApply(universityInfo)
                .create();
    }
}
