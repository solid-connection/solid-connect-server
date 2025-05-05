package com.example.solidconnection.support.fixture;

import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.LanguageRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LanguageRequirementFixture {

    private final LanguageRequirementRepository languageRequirementRepository;

    public LanguageRequirementBuilder languageRequirement() {
        return new LanguageRequirementBuilder();
    }

    public class LanguageRequirementBuilder {

        private LanguageTestType languageTestType;
        private String minScore;
        private UniversityInfoForApply universityInfoForApply;

        public LanguageRequirementBuilder languageTestType(LanguageTestType languageTestType) {
            this.languageTestType = languageTestType;
            return this;
        }

        public LanguageRequirementBuilder minScore(String minScore) {
            this.minScore = minScore;
            return this;
        }

        public LanguageRequirementBuilder universityInfoForApply(UniversityInfoForApply universityInfoForApply) {
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
            return languageRequirementRepository.save(languageRequirement);
        }
    }
}
