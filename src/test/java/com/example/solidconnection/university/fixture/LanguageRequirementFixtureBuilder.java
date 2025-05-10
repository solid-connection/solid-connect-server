package com.example.solidconnection.university.fixture;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class LanguageRequirementFixtureBuilder {

    private final LanguageRequirementRepository languageRequirementRepository;

    private LanguageTestType languageTestType;
    private String minScore;
    private UniversityInfoForApply universityInfoForApply;

    public LanguageRequirementFixtureBuilder languageTestType(LanguageTestType languageTestType) {
        this.languageTestType = languageTestType;
        return this;
    }

    public LanguageRequirementFixtureBuilder minScore(String minScore) {
        this.minScore = minScore;
        return this;
    }

    public LanguageRequirementFixtureBuilder universityInfoForApply(UniversityInfoForApply universityInfoForApply) {
        this.universityInfoForApply = universityInfoForApply;
        return this;
    }

    public LanguageRequirement create() {
        LanguageRequirement languageRequirement = new LanguageRequirement(
                null,
                languageTestType,
                minScore,
                universityInfoForApply
        );
        universityInfoForApply.addLanguageRequirements(languageRequirement);
        return languageRequirementRepository.save(languageRequirement);
    }
}
