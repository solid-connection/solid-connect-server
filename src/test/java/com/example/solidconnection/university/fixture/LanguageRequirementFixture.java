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

    public LanguageRequirement 토플_80(UnivApplyInfo univApplyInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("80")
                .univApplyInfo(univApplyInfo)
                .create();
    }

    public LanguageRequirement 토플_70(UnivApplyInfo univApplyInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("70")
                .univApplyInfo(univApplyInfo)
                .create();
    }

    public LanguageRequirement 토익_800(UnivApplyInfo univApplyInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .univApplyInfo(univApplyInfo)
                .create();
    }

    public LanguageRequirement 토익_900(UnivApplyInfo univApplyInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("900")
                .univApplyInfo(univApplyInfo)
                .create();
    }

    public LanguageRequirement JLPT_N2(UnivApplyInfo univApplyInfo) {
        return languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.JLPT)
                .minScore("N2")
                .univApplyInfo(univApplyInfo)
                .create();
    }
}
