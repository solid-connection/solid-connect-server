package com.example.solidconnection.university.fixture;

import static com.example.solidconnection.university.domain.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.university.domain.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UnivApplyInfoFixtureBuilder {

    private final UnivApplyInfoRepository univApplyInfoRepository;

    private String term;
    private String koreanName;
    private University university;

    public UnivApplyInfoFixtureBuilder univApplyInfo() {
        return new UnivApplyInfoFixtureBuilder(univApplyInfoRepository);
    }

    public UnivApplyInfoFixtureBuilder term(String term) {
        this.term = term;
        return this;
    }

    public UnivApplyInfoFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public UnivApplyInfoFixtureBuilder university(University university) {
        this.university = university;
        return this;
    }

    public UnivApplyInfo create() {
        UnivApplyInfo univApplyInfo = new UnivApplyInfo(
                null, term, koreanName, 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), university
        );
        return univApplyInfoRepository.save(univApplyInfo);
    }
}
