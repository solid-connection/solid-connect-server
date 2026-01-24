package com.example.solidconnection.university.fixture;

import static com.example.solidconnection.university.domain.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.university.domain.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

import com.example.solidconnection.university.domain.UnivApplyInfo;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.UnivApplyInfoRepository;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UnivApplyInfoFixtureBuilder {

    private final UnivApplyInfoRepository univApplyInfoRepository;

    private long termId;
    private String koreanName;
    private HostUniversity university;

    public UnivApplyInfoFixtureBuilder univApplyInfo() {
        return new UnivApplyInfoFixtureBuilder(univApplyInfoRepository);
    }

    public UnivApplyInfoFixtureBuilder termId(long termId) {
        this.termId = termId;
        return this;
    }

    public UnivApplyInfoFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public UnivApplyInfoFixtureBuilder university(HostUniversity university) {
        this.university = university;
        return this;
    }

    public UnivApplyInfo create() {
        UnivApplyInfo univApplyInfo = new UnivApplyInfo(
                null, termId, koreanName, 1, HOME_UNIVERSITY_PAYMENT, ONE_SEMESTER,
                "1", "detailsForLanguage", "gpaRequirement",
                "gpaRequirementCriteria", "detailsForApply", "detailsForMajor",
                "detailsForAccommodation", "detailsForEnglishCourse", "details",
                new HashSet<>(), university
        );
        return univApplyInfoRepository.save(univApplyInfo);
    }
}
