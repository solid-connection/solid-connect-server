package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import com.example.solidconnection.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryFixture {

    private final CountryRepository countryRepository;

    public CountryBuilder country() {
        return new CountryBuilder();
    }

    public class CountryBuilder {

        private String code;
        private String koreanName;
        private Region region;

        public CountryBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CountryBuilder koreanName(String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        public CountryBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public Country create() {
            Country country = new Country(code, koreanName, region);
            return countryRepository.save(country);
        }
    }
}
