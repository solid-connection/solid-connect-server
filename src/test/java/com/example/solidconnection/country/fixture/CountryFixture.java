package com.example.solidconnection.country.fixture;

import com.example.solidconnection.location.country.domain.Country;
import com.example.solidconnection.region.fixture.RegionFixture;
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

    public Country 캐나다() {
        return countryFixtureBuilder.country()
                .code("CA")
                .koreanName("캐나다")
                .region(regionFixture.영미권())
                .findOrCreate();
    }

    public Country 덴마크() {
        return countryFixtureBuilder.country()
                .code("DK")
                .koreanName("덴마크")
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public Country 오스트리아() {
        return countryFixtureBuilder.country()
                .code("AT")
                .koreanName("오스트리아")
                .region(regionFixture.유럽())
                .findOrCreate();
    }

    public Country 일본() {
        return countryFixtureBuilder.country()
                .code("JP")
                .koreanName("일본")
                .region(regionFixture.아시아())
                .findOrCreate();
    }
}
