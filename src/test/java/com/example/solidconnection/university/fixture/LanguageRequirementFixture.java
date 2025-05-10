package com.example.solidconnection.university.fixture;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LanguageRequirementFixture {

    private final LanguageRequirementFixtureBuilder languageRequirementFixtureBuilder;

    public UniversityInfoForApply 괌대학_A_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("80")
                .universityInfoForApply(universityInfo)
                .create();
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 괌대학_B_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("70")
                .universityInfoForApply(universityInfo)
                .create();
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("900")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 네바다주립_대학_라스베이거스_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 메모리얼_대학_세인트존스_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 서던덴마크_대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("70")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 코펜하겐IT대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("80")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 그라츠대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEFL_IBT)
                .minScore("80")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 그라츠공과대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 린츠_카톨릭대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.TOEIC)
                .minScore("800")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }

    public UniversityInfoForApply 메이지대학_언어요구사항(UniversityInfoForApply universityInfo) {
        languageRequirementFixtureBuilder
                .languageTestType(LanguageTestType.JLPT)
                .minScore("N2")
                .universityInfoForApply(universityInfo)
                .create();
        return universityInfo;
    }
}
