package com.example.solidconnection.support.fixture;

import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.type.LanguageTestType;
import com.example.solidconnection.type.SemesterAvailableForDispatch;
import com.example.solidconnection.type.TuitionFeeType;
import com.example.solidconnection.university.domain.LanguageRequirement;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;

import java.util.HashSet;
import java.util.Set;

import static com.example.solidconnection.custom.exception.ErrorCode.COUNTRY_NOT_FOUND_BY_KOREAN_NAME;
import static com.example.solidconnection.custom.exception.ErrorCode.REGION_NOT_FOUND_BY_KOREAN_NAME;
import static com.example.solidconnection.custom.exception.ErrorCode.UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND;

public final class UniversityFixture {

    private final BuilderSupporter bs;

    private Region region;
    private Country country;
    private University university;
    private UniversityInfoForApply universityInfoForApply;
    private Set<LanguageRequirement> languageRequirements = new HashSet<>();

    public UniversityFixture(BuilderSupporter bs) {
        this.bs = bs;
    }

    public UniversityFixture 지역을_생성한다(String code, String koreanName) {
        Region region = new Region(code, koreanName);
        this.region = bs.regionRepository().save(region);
        return this;
    }

    public UniversityFixture 국가를_생성한다(String code, String koreanName) {
        if (this.region == null) {
            throw new CustomException(REGION_NOT_FOUND_BY_KOREAN_NAME);
        }
        Country country = new Country(code, koreanName, this.region);
        this.country = bs.countryRepository().save(country);
        return this;
    }

    public UniversityFixture 대학을_생성한다(
            String koreanName,
            String englishName,
            String formatName,
            String homepageUrl,
            String englishCourseUrl,
            String accommodationUrl,
            String logoImageUrl,
            String backgroundImageUrl,
            String detailsForLocal) {
        if (this.country == null) {
            throw new CustomException(COUNTRY_NOT_FOUND_BY_KOREAN_NAME);
        }
        University university = new University(
                null,
                koreanName,
                englishName,
                formatName,
                homepageUrl,
                englishCourseUrl,
                accommodationUrl,
                logoImageUrl,
                backgroundImageUrl,
                detailsForLocal,
                this.country,
                this.region);
        this.university = bs.universityRepository().save(university);
        return this;
    }

    public UniversityFixture 대학_지원_정보를_생성한다(
            String term,
            String koreanName,
            Integer studentCapacity,
            TuitionFeeType tuitionFeeType,
            SemesterAvailableForDispatch semesterAvailableForDispatch,
            String semesterRequirement,
            String detailsForLanguage,
            String gpaRequirement,
            String gpaRequirementCriteria,
            String detailsForApply,
            String detailsForMajor,
            String detailsForAccommodation,
            String detailsForEnglishCourse,
            String details) {
        if (this.university == null) {
            throw new CustomException(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND);
        }
        UniversityInfoForApply universityInfoForApply = new UniversityInfoForApply(
                null,
                term,
                koreanName,
                studentCapacity,
                tuitionFeeType,
                semesterAvailableForDispatch,
                semesterRequirement,
                detailsForLanguage,
                gpaRequirement,
                gpaRequirementCriteria,
                detailsForApply,
                detailsForMajor,
                detailsForAccommodation,
                detailsForEnglishCourse,
                details,
                new HashSet<>(),
                this.university);
        this.universityInfoForApply = bs.universityInfoForApplyRepository().save(universityInfoForApply);
        return this;
    }

    public UniversityFixture 언어_요구사항을_추가한다(LanguageTestType testType, String minScore) {
        if (this.universityInfoForApply == null) {
            throw new CustomException(UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND);
        }
        LanguageRequirement languageRequirement = new LanguageRequirement(
                null,
                testType,
                minScore,
                this.universityInfoForApply);
        this.universityInfoForApply.addLanguageRequirements(languageRequirement);
        bs.universityInfoForApplyRepository().save(this.universityInfoForApply);
        LanguageRequirement saved = bs.languageRequirementRepository().save(languageRequirement);
        this.languageRequirements.add(saved);
        return this;
    }

    public Region 지역() {
        return region;
    }

    public Country 국가() {
        return country;
    }

    public University 대학() {
        return university;
    }

    public UniversityInfoForApply 대학_지원_정보() {
        return universityInfoForApply;
    }

    public Set<LanguageRequirement> 언어_요구사항들() {
        return languageRequirements;
    }
}
