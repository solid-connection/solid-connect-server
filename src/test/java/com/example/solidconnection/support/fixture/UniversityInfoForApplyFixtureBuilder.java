package com.example.solidconnection.support.fixture;

import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

import java.util.HashSet;

import static com.example.solidconnection.type.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.type.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

@TestComponent
@RequiredArgsConstructor
public class UniversityInfoForApplyFixtureBuilder {

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;

    private String term;
    private String koreanName;
    private University university;

    public UniversityInfoForApplyFixtureBuilder universityInfoForApply() {
        return new UniversityInfoForApplyFixtureBuilder(universityInfoForApplyRepository);
    }

    public UniversityInfoForApplyFixtureBuilder term(String term) {
        this.term = term;
        return this;
    }

    public UniversityInfoForApplyFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public UniversityInfoForApplyFixtureBuilder university(University university) {
        this.university = university;
        return this;
    }

    public UniversityInfoForApply create() {
        UniversityInfoForApply universityInfoForApply = new UniversityInfoForApply(
                null, term, koreanName, 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), university
        );
        return universityInfoForApplyRepository.save(universityInfoForApply);
    }
}
