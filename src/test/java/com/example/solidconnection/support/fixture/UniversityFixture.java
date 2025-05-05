package com.example.solidconnection.support.fixture;

import com.example.solidconnection.university.domain.University;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class UniversityFixture {

    private final RegionFixture regionFixture;
    private final CountryFixture countryFixture;
    private final UniversityFixtureBuilder universityFixtureBuilder;

    public University 괌_대학() {
        return universityFixtureBuilder.university()
                .koreanName("괌 대학")
                .englishName("University of Guam")
                .country(countryFixture.미국())
                .region(regionFixture.영미권())
                .create();
    }
}
