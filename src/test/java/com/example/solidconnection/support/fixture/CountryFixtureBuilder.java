package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import com.example.solidconnection.entity.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class CountryFixtureBuilder {

    private final CountryRepositoryForTest countryRepositoryForTest;

    private String code;
    private String koreanName;
    private Region region;

    public CountryFixtureBuilder country() {
        return new CountryFixtureBuilder(countryRepositoryForTest);
    }

    public CountryFixtureBuilder code(String code) {
        this.code = code;
        return this;
    }

    public CountryFixtureBuilder koreanName(String koreanName) {
        this.koreanName = koreanName;
        return this;
    }

    public CountryFixtureBuilder region(Region region) {
        this.region = region;
        return this;
    }

    public Country findOrCreate() {
        return countryRepositoryForTest.findByCode(code)
                .orElseGet(() -> countryRepositoryForTest.save(new Country(code, koreanName, region)));
    }
}
