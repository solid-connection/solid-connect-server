package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.university.domain.University;
import com.example.solidconnection.university.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class UniversityFixture {

    private final UniversityRepository universityRepository;

    public UniversityBuilder university() {
        return new UniversityBuilder();
    }

    public class UniversityBuilder {

        private String koreanName;
        private String englishName;
        private String formatName;
        private Country country;
        private Region region;

        public UniversityBuilder koreanName(String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        public UniversityBuilder englishName(String englishName) {
            this.englishName = englishName;
            return this;
        }

        public UniversityBuilder formatName(String formatName) {
            this.formatName = formatName;
            return this;
        }

        public UniversityBuilder country(Country country) {
            this.country = country;
            return this;
        }

        public UniversityBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public University create() {
            University university = new University(
                    null, koreanName, englishName, formatName,
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
}
