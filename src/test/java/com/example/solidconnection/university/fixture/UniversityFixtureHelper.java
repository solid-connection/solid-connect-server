package com.example.solidconnection.university.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.support.fixture.CountryFixtureBuilder;
import com.example.solidconnection.support.fixture.LanguageRequirementFixture;
import com.example.solidconnection.support.fixture.RegionFixtureBuilder;
import com.example.solidconnection.support.fixture.UniversityFixtureBuilder;
import com.example.solidconnection.support.fixture.UniversityInfoForApplyFixtureBuilder;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UniversityFixtureHelper {

    private final CountryFixtureBuilder countryFixture;
    private final RegionFixtureBuilder regionFixture;
    private final UniversityFixtureBuilder universityFixture;
    private final UniversityInfoForApplyFixtureBuilder universityInfoForApplyFixture;
    private final LanguageRequirementFixture languageRequirementFixture;

    @Value("${university.term}")
    public String term;

    public UniversityData 괌대학_A_생성() {
        return 대학_생성(
                "AMERICAS", "영미권",
                "US", "미국",
                "괌대학", "University of Guam", "university_of_guam",
                "괌대학(A형)",
                Map.of(
                        LanguageTestType.TOEFL_IBT, "80",
                        LanguageTestType.TOEIC, "800"
                )
        );
    }

    private UniversityData 대학_생성(
            String regionCode,
            String regionKoreanName,
            String countryCode,
            String countryKoreanName,
            String universityKoreanName,
            String universityEnglishName,
            String universityFormatName,
            String infoKoreanName,
            Map<LanguageTestType, String> languageRequirementMap) {

        Region region = regionFixture.region()
                .code(regionCode)
                .koreanName(regionKoreanName)
                .findOrCreate();

        Country country = countryFixture.country()
                .code(countryCode)
                .koreanName(countryKoreanName)
                .region(region)
                .findOrCreate();

        University university = universityFixture.university()
                .koreanName(universityKoreanName)
                .englishName(universityEnglishName)
                .country(country)
                .region(region)
                .create();

        UniversityInfoForApply universityInfoForApply = universityInfoForApplyFixture.universityInfoForApply()
                .term(term)
                .koreanName(infoKoreanName)
                .university(university)
                .create();

        Set<LanguageRequirement> languageRequirements = new HashSet<>();
        for (Map.Entry<LanguageTestType, String> entry : languageRequirementMap.entrySet()) {
            LanguageRequirement languageRequirement = languageRequirementFixture.languageRequirement()
                    .languageTestType(entry.getKey())
                    .minScore(entry.getValue())
                    .universityInfoForApply(universityInfoForApply)
                    .create();
            languageRequirements.add(languageRequirement);
            universityInfoForApply.addLanguageRequirements(languageRequirement);
        }

        return new UniversityData(
                region,
                country,
                university,
                universityInfoForApply,
                languageRequirements
        );
    }
}
