package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UniversityFixtureBuilder {

    private final UniversityRepository universityRepository;

    private String koreanName;
    private String englishName;
    private Country country;
    private Region region;

    public UniversityFixtureBuilder university() {
        return new UniversityFixtureBuilder(universityRepository);
    }

    public UniversityFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public UniversityFixtureBuilder englishName(String englishName) {
        this.englishName = englishName;
        return this;
    }

    public UniversityFixtureBuilder country(Country country) {
        this.country = country;
        return this;
    }

    public UniversityFixtureBuilder region(Region region) {
        this.region = region;
        return this;
    }

    public University create() {
        University university = new University(
                null, koreanName, englishName,
                "formatName",
                "https://homepage-url",
                "https://english-course-url",
                "https://accommodation-url",
                "https://logo-image-url",
                "https://background-image-url",
                null, country, region
        );
        return universityRepository.save(university);
    }
}
