package com.example.solidconnection.support.fixture;

import com.example.solidconnection.entity.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class CountryFixture {

    private final RegionFixture regionFixture;
    private final CountryFixtureBuilder countryFixtureBuilder;

    public Country 미국() {
        return countryFixtureBuilder.country()
                .code("US")
                .koreanName("미국")
                .region(regionFixture.영미권())
                .findOrCreate();
    }
}
