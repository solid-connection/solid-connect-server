package com.example.solidconnection.university.fixture;

import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.location.region.domain.Region;
import com.example.solidconnection.university.domain.HostUniversity;
import com.example.solidconnection.university.repository.HostUniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class UniversityFixtureBuilder {

    private final HostUniversityRepository hostUniversityRepository;

    private String koreanName;
    private String englishName;
    private Country country;
    private Region region;

    public UniversityFixtureBuilder university() {
        return new UniversityFixtureBuilder(hostUniversityRepository);
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

    public HostUniversity create() {
        HostUniversity university = new HostUniversity(
                null, koreanName, englishName,
                "formatName",
                "https://homepage-url",
                "https://english-course-url",
                "https://accommodation-url",
                "https://logo-image-url",
                "https://background-image-url",
                null, country, region
        );
        return hostUniversityRepository.save(university);
    }
}
