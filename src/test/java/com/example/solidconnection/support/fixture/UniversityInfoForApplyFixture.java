package com.example.solidconnection.support.fixture;

import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;

import static com.example.solidconnection.type.SemesterAvailableForDispatch.ONE_SEMESTER;
import static com.example.solidconnection.type.TuitionFeeType.HOME_UNIVERSITY_PAYMENT;

@Component
@RequiredArgsConstructor
public class UniversityInfoForApplyFixture {

    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;

    public UniversityInfoForApplyBuilder universityInfoForApply() {
        return new UniversityInfoForApplyBuilder();
    }

    public class UniversityInfoForApplyBuilder {

        private String term;
        private String koreanName;
        private University university;

        public UniversityInfoForApplyBuilder term(String term) {
            this.term = term;
            return this;
        }

        public UniversityInfoForApplyBuilder koreanName(String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        public UniversityInfoForApplyBuilder university(University university) {
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
}
